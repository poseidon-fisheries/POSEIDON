library(tidyverse)
library(ncdf4)
library(fs)
library(lubridate)
library(here)
source(here("scripts", "extract_ncdf_data.R"))

bounds <-
  read_csv("antigua_convention_area.csv") %>%
  summarise(min_lon = min(x), max_lon = max(x), min_lat = min(y), max_lat = max(y))

nc_file_dir <- path_home("Desktop") # for now...
date <- ymd("2016-09-29")
nc_file_name <- str_glue("hycom_GLBv0.08_563_", format(date, "%Y%m%d"), "12_t000.nc")
nc_file_path <- path(nc_file_dir, nc_file_name)
if (!file_exists(nc_file_path)) {
  base_url <- str_glue("ftp://ftp.hycom.org/datasets/GLBv0.08/expt_56.3/data/", year(date), "/")
  download.file(str_glue(base_url, nc_file_name), nc_file_path, mode = "wb")
}

nc <- nc_open(nc_file_path)
df <-
  extract_current_vectors(nc, "water_u", "water_v") %>%
  mutate(component = str_sub(component, -1)) %>%
  filter(
    between(lat, bounds$min_lat, bounds$max_lat),
    between(lon, bounds$min_lon, bounds$max_lon)
  )
nc_close(nc)

df %>%
  spread(component, value) %>%
  arrange(lon, lat) %>%
  write_csv(here("currents.csv"))

df %>%
  filter(between(lat, -20, 20)) %>%
  ggplot() +
  geom_tile(aes(x = lon, y = lat, fill = value)) +
  coord_fixed() +
  facet_wrap(vars(component)) +
  scale_fill_distiller(palette = "RdBu") +
  theme_minimal()
