library(here)
source(here("scripts", "download_depth_df.R"))

library(tidyverse)
library(fs)
library(readxl)
library(geosphere)
library(digest)

input_path <- path_home("Dropbox", "POSEIDON", "FAD Buoy Data_Mine")

df_events <-
  path(input_path, "FAD_Deployments.xlsx") %>%
  read_excel() %>%
  semi_join(., filter(., FADType == "Normal"), by = "M3i num") %>%
  transmute(
    buoy_id = as.integer(`M3i num`),
    lat = Lat,
    lon = Lon,
    date_time = DateTime,
    event_type = fct_recode(as.character(EventID), Deploy = "1", Redeploy = "5")
  ) %>%
  drop_na()

df_readings <-
  dir_ls(input_path, glob = "*.csv") %>%
  map(~ read_delim(., ";",
    na = c("", "---", "----"),
    col_types = cols(
      NOMBRE = col_character(),
      ALIAS = col_skip(),
      GRUPO = col_skip(),
      FECHA = col_datetime(format = "%d/%m/%Y %H:%M"),
      LATITUD = col_double(),
      LONGITUD = col_double(),
      FLASH = col_skip(),
      VOLTAJE = col_skip(),
      TEMPERATURA = col_skip(),
      VELOCIDAD = col_number(),
      RUMBO = col_skip()
    )
  )) %>%
  bind_rows() %>%
  transmute(
    buoy_id = as.integer(str_extract(NOMBRE, "\\d{2,}")),
    date_time = FECHA,
    lon = LONGITUD,
    lat = LATITUD,
    velocity = VELOCIDAD * 0.514444 # convert knots to m/s
  ) %>%
  semi_join(df_events, by = "buoy_id") %>%
  mutate(buoy_id = as_factor(as.character(buoy_id)))

breaks <-
  df_readings %>%
  arrange(date_time) %>%
  top_n(3, date_time - lag(date_time)) %>%
  pull(date_time) %>%
  c(min(df_readings$date_time), max(df_readings$date_time))

df <-
  df_readings %>%
  mutate(
    period = cut(date_time, breaks, include.lowest = TRUE),
    period = fct_relabel(period, ~ str_sub(., 1, 7))
  ) %>%
  group_by(buoy_id, period) %>%
  group_map(~ .x %>%
    arrange(date_time) %>%
    rowid_to_column() %>%
    mutate(.,
      dist = pmap_dbl(list(lon, lat, lag(lon), lag(lat)), ~ distHaversine(c(..1, ..2), c(..3, ..4))),
      i = .bincode(rowid, c(-Inf, filter(., dist > 75E4)$rowid, Inf), right = FALSE),
      segment = map_chr(paste0(.y$buoy_id, .y$period, i), digest)
    ) %>%
    select(date_time, lon, lat, segment)) %>%
  group_by(segment) %>%
  filter(n() > 1) %>%
  ungroup()

df %>%
  select(segment, date_time, lon, lat) %>%
  write_csv(here("empirical_fad_trajectories.csv"))

df %>%
  group_by(segment) %>%
  arrange(date_time) %>%
  summarise(
    lon = first(lon),
    lat = first(lat),
    start = first(date_time),
    duration = floor(as.numeric(difftime(last(date_time), start), units = "days"))
  ) %>%
  filter(duration >= 1) %>%
  write_csv(here("fad_trajectories_validation_cases.csv"))

# From here on, it's plots... -------------------------------------------------------

# Read the currents file so we can get a visual comparison with the velocity readings
df_currents <-
  read_csv("currents.csv") %>%
  filter(
    between(lon, min(df_readings$lon), max(df_readings$lon)),
    between(lat, min(df_readings$lat), max(df_readings$lat))
  ) %>%
  transmute(source = "currents", velocity = sqrt(u^2 + v^2))

# Make a density plot of the velocity readings against the currents velocity
df_readings %>%
  filter(velocity <= 2) %>% # we're chucking out about 4% of readings here
  transmute(source = "readings", velocity) %>%
  full_join(df_currents) %>%
  ggplot(aes(x = velocity, color = source, fill = source)) +
  geom_density(alpha = 0.3, adjust = 2) +
  theme_minimal()

df %>%
  ggplot(aes(x = date_time, y = buoy_id, color = period)) +
  geom_point(alpha = 0.1) +
  scale_color_viridis_d() +
  guides(colour = guide_legend(override.aes = list(alpha = 1))) +
  theme_classic()

df %>%
  ggplot(aes(x = date_time, y = buoy_id, color = segment)) +
  geom_point(alpha = 0.5) +
  facet_wrap(vars(period), scales = "free") +
  guides(colour = FALSE) +
  theme_minimal() +
  theme(panel.grid.major.y = element_blank())

depth_df <-
  download_depth_df(NA, min(df$lon), min(df$lat), max(df$lon), max(df$lat), .1) %>%
  drop_na()

df %>%
  arrange(date_time) %>%
  ggplot(aes(x = lon, y = lat)) +
  geom_tile(data = depth_df, aes(x = x, y = y, fill = depth)) +
  geom_path(aes(group = segment, color = as.integer(as_factor(segment))), alpha = 0.6) +
  facet_wrap(vars(period)) +
  scale_colour_gradient(low = "yellow", high = "orange") +
  guides(fill = FALSE, colour = FALSE) +
  coord_fixed() +
  theme_minimal()

df %>%
  group_by(segment) %>%
  filter(n() > 10) %>%
  ungroup() %>%
  filter(segment %in% sample(segment, 12)) %>%
  arrange(date_time) %>%
  ggplot(aes(x = lon, y = lat)) +
  geom_path(aes(group = segment), alpha = 0.6) +
  facet_wrap(vars(segment)) +
  coord_fixed() +
  theme_minimal()
