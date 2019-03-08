library(tidyverse)
library(geosphere)

df_sim <-
  read_csv("simulated_fad_trajectories.csv", col_types = "cTdd") %>%
  mutate(source = "sim")

df_emp <-
  read_csv("empirical_fad_trajectories.csv", col_types = "cTdd") %>%
  mutate(source = "emp")

# Bind the two data frames together, arranging the simulated trajectories such that
# there is a row for each time step from the empirical trajectory recording where
# the simulated FAD would have been at that time (without any interpolation)
df <-
  group_by(df_sim, segment) %>%
  arrange(date_time, .by_group = TRUE) %>%
  group_map(~ {
    breaks <- c(.x$date_time, Inf)
    sim <- .x %>% mutate(dt = cut(date_time, breaks, include.lowest = TRUE))
    emp <- df_emp %>% filter(segment == .y$segment) %>% select(-segment)
    emp %>%
      mutate(dt = cut(date_time, breaks, include.lowest = TRUE)) %>%
      left_join(sim, by = "dt") %>%
      select(date_time = date_time.x, lon = lon.y, lat = lat.y, source = source.y) %>%
      bind_rows(emp)
  })

df %>% nest() %>% sample_n(20) %>% unnest() %>%
  ggplot(aes(lon, lat, group = source, color = source)) +
  geom_path() +
  facet_wrap(vars(segment)) +
  coord_fixed() +
  theme_minimal()

