library(tidyverse)
library(fs)
library(readxl)
library(geosphere)
library(here)

source(here("scripts", "download_depth_df.R"))

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
      VELOCIDAD = col_skip(),
      RUMBO = col_skip()
    )
  )) %>%
  bind_rows() %>%
  transmute(
    buoy_id = as.integer(str_extract(NOMBRE, "\\d{2,}")),
    date_time = FECHA,
    lon = LONGITUD,
    lat = LATITUD
  ) %>%
  semi_join(df_events, by = "buoy_id") %>%
  mutate(buoy_id = as_factor(as.character(buoy_id)))

breaks <-
  df_readings %>%
  arrange(date_time) %>%
  top_n(3, date_time - lag(date_time)) %>%
  pull(date_time) %>%
  c(min(df_readings$date_time), max(df_readings$date_time))

df_with_periods <-
  mutate(df_readings, period = cut(date_time, breaks, include.lowest = TRUE))

df_with_periods %>%
  ggplot(aes(x = date_time, y = buoy_id, color = period)) +
  geom_point(alpha = 0.1) +
  theme_classic()

get_depth_df <- function(df, res) {
  download_depth_df(NA, min(df$lon), min(df$lat), max(df$lon), max(df$lat), res) %>% drop_na()
}
depth_df <- get_depth_df(df_with_periods, .1)

df <-
  df_with_periods %>%
  group_by(buoy_id, period) %>%
  group_map(~ .x %>%
    arrange(date_time) %>%
    rowid_to_column() %>%
    mutate(dist = pmap_dbl(
      list(lon, lat, lag(lon), lag(lat)),
      ~ distHaversine(c(..1, ..2), c(..3, ..4))
    )) %>%
    mutate(., segment =
      .bincode(rowid, c(-Inf, filter(., dist > 75E4)$rowid, Inf), right = FALSE)
    ) %>%
    select(date_time, lon, lat, segment)
  ) %>%
  ungroup() %>%
  mutate(segment = fct_cross(buoy_id, as_factor(segment), sep = "-")) %>%
  group_by(segment) %>%
  filter(n() > 1) %>%
  ungroup() %>%
  droplevels()

df %>%
  arrange(date_time) %>%
  ggplot(aes(x = lon, y = lat)) +
  geom_tile(data = depth_df, aes(x = x, y = y, fill = depth)) +
  geom_path(aes(group = segment, color = as.integer(segment)), alpha = 0.6) +
  facet_wrap(vars(period)) +
  scale_colour_gradient(low = "yellow", high = "orange") +
  guides(fill = FALSE, colour = FALSE) +
  coord_fixed() +
  theme_minimal()

df %>%
  transmute(segment = as.integer(segment), date_time, lon, lat) %>%
  write_csv(here("fad_trajectories.csv"))
