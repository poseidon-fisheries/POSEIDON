library(maps)
library(tidyverse)
library(geosphere)
library(fuzzyjoin)
library(here)

# This is basically `geosphere::midPoint` with an arbitrary fraction instead of 0.5.
interpolate <- function(p1, p2, fraction) {
  gi <- geodesic_inverse(p1, p2)
  b <- gi[, "azimuth1"]
  d <- gi[, "distance"] * fraction
  destPoint(p1, b, d)
}

align <- function(df, ref_df, pb) {
  pb$tick()$print()
  df %>%
    transmute(
      t1 = date_time, lon1 = lon, lat1 = lat,
      t2 = lead(date_time, order_by = date_time), lon2 = lead(lon), lat2 = lead(lat)
    ) %>%
    drop_na() %>%
    interval_inner_join(transmute(ref_df, t1 = date_time, t2 = date_time), by = c("t1", "t2")) %>%
    group_by(t1.y) %>%
    slice(1) %>% # interval_inner_join is inclusive at both ends so we need to remove dups for exact sim/emp matches
    mutate(
      fraction = as.double(t1.y - t1.x, units = "secs") / as.double(t2.x - t1.x, units = "secs"),
      p = pmap(list(map2(lon1, lat1, c), map2(lon2, lat2, c), fraction), interpolate)
    ) %>%
    bind_cols(., map_df(.$p, as_tibble)) %>%
    select(date_time = t1.y, lon, lat)
}

df_sim <-
  tibble(
    source = c("seapodym_2010", "seapodym_2016_1", "seapodym_2016_2", "hycom_2016")
  ) %>%
  mutate(
    file = here(str_glue_data(., "simulated_fad_trajectories_{source}.csv")),
    df = map(file, ~ read_csv(., col_types = "cTdd"))
  ) %>%
  select(-file) %>%
  unnest()

df_emp <-
  read_csv(here("empirical_fad_trajectories.csv"), col_types = "cTdd") %>%
  semi_join(df_sim, by = "trajectory") %>%
  group_by(trajectory, date_time) %>%
  slice(1) %>% # some trajectories have >1 readings for the same dtm; just keep the first
  group_by(trajectory) %>%
  group_map(~ align(.x, filter(df_sim, trajectory == .y$trajectory), pb = progress_estimated(nrow(.x)))) %>%
  filter(n() > 1) %>%
  ungroup()

df_skill <-
  df_sim %>%
  nest(-source, -trajectory) %>%
  nest_join(df_emp) %>%
  filter(map(df_emp, nrow) > 0) %>%
  mutate(skill = map2(data, df_emp, function(df_sim, df_emp) {
    tolerance <- 50
    # http://ocgweb.marine.usf.edu/~liu/Papers/Liu_etal_2014_JGR.pdf
    df_sim %>%
      inner_join(df_emp, by = "date_time", suffix = c("_sim", "_emp")) %>%
      arrange(date_time) %>%
      mutate(
        separation = distHaversine(cbind(lon_emp, lat_emp), cbind(lon_sim, lat_sim)),
        length = distHaversine(cbind(lon_emp, lat_emp), cbind(lag(lon_emp), lag(lat_emp))) %>% replace_na(0)
      ) %>%
      summarise(
        sep = sum(separation),
        len = sum(length),
        c = sep / len,
        skill = max(0, 1 - (c / tolerance))
      )
  })) %>%
  unnest(skill)

df_skill %>%
  filter(c < 1000) %>%
  ggplot(aes(c, group = source, fill = source)) + geom_histogram(position = "dodge")

df_skill %>%
  filter(c < 1000) %>%
  ggplot(aes(c, x = len / 1000, y = sep / 1000, color = source)) +
  geom_point(alpha = 0.75)

df_skill %>%
  filter(c < 1000) %>%
  ggplot(aes(source, c, group = source, fill = source)) +
  scale_y_log10() +
  geom_boxplot(outlier.shape = NA) +
  geom_jitter(width = 0.1, alpha = 0.25)

df_skill %>%
  semi_join(df_skill %>% nest(-source) %>% head(1) %>% unnest() %>% top_n(6, len) %>% select(trajectory)) %>%
  select(source, trajectory, data) %>%
  unnest() %>%
  bind_rows(mutate(semi_join(df_emp, ., by = "trajectory"), source = "emp")) %>%
  {
    ggplot(., aes(lon, lat, group = source, color = source)) +
      geom_path() +
      geom_point(alpha = 0.2) +
      facet_wrap(vars(trajectory), ncol = 2) +
      coord_quickmap(xlim = c(min(.$lon), max(.$lon)), ylim = c(min(.$lat), max(.$lat))) +
      borders() +
      theme_minimal()
  }
