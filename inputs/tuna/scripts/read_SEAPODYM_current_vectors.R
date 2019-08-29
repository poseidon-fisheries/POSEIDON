library(maps)
library(tidyverse)
library(ncdf4)
library(fs)
library(lubridate)
library(here)
library(ggquiver)
library(gganimate)

source(here("scripts", "extract_ncdf_data.R"))

base_path <- path_home("Dropbox", "POSEIDON", "Inputs", "SEAPODYM")
limits <- read_csv("limits.csv")
df <-
  tibble(var = c("U", "V")) %>%
  mutate(nc = map(var, ~ dir_map(path(base_path, .), nc_open))) %>%
  unnest() %>%
  transmute(
    dttm = as_datetime(map_dbl(nc, ~ ncvar_get(., "time"))),
    ncdf = map2(nc, var, function(nc, var, pb) {
      extract_ncdf_matrix(nc, var, "longitude", "latitude") %T>% {
        nc_close(nc)
        pb$tick()$print()
      } %>%
        transmute(lon = longitude - 360, lat = latitude, value) %>%
        filter(between(lon, limits$west, limits$east), between(lat, limits$south, limits$north))
    }, pb = progress_estimated(n())),
    var = tolower(var)
  ) %>%
  unnest() %>%
  spread(var, value)

df %>%
  filter(dttm == as_date("2016-09-28")) %>%
  write_csv("currents_seapodym_2016_1.csv")

df %>%
  write_csv("currents_seapodym_2016_2.csv")

df %>%
  filter(dttm == as_date("2016-09-28")) %>%
  ggplot() +
  theme_minimal() +
  geom_tile(aes(x = lon, y = lat, fill = u)) +
  borders(fill = "green4", alpha = 0.1) +
  coord_quickmap(xlim = c(limits$west, limits$east), ylim = c(limits$south, limits$north)) +
  scale_y_continuous(expand = c(0, 0)) + scale_x_continuous(expand = c(0, 0)) +
  scale_fill_viridis_c()

anim <-
  df %>%
  filter(abs(lat) <= 25) %>%
  glimpse() %>%
  ggplot(aes(lon, lat, u = u, v = v, color = u)) +
  theme_minimal() +
  borders(fill = "green4", alpha = 0.1) +
  geom_quiver() +
  coord_quickmap(xlim = c(limits$west, limits$east), ylim = c(-25, 25)) +
  scale_y_continuous(expand = c(0, 0)) + scale_x_continuous(expand = c(0, 0)) +
  scale_colour_viridis_c() +
  labs(title = 'Date: {frame_time}') +
  transition_time(dttm)

anim %>% animate(nframes = df$dttm %>% n_distinct())
