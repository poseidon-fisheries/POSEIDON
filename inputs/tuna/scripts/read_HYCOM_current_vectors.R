library(maps)
library(tidyverse)
library(ncdf4)
library(fs)
library(lubridate)
library(here)
source(here("scripts", "extract_ncdf_data.R"))

nc_file_dir <- here("raw")
date <- ymd("2016-09-29")
nc_file_name <- str_glue("hycom_GLBv0.08_563_", format(date, "%Y%m%d"), "12_t000.nc")
nc_file_path <- path(nc_file_dir, nc_file_name)
if (!file_exists(nc_file_path)) {
  base_url <- str_glue("ftp://ftp.hycom.org/datasets/GLBv0.08/expt_56.3/data/", year(date), "/")
  download.file(str_glue(base_url, nc_file_name), nc_file_path, mode = "wb")
}

nc <- nc_open(nc_file_path)
nc_df <- extract_current_vectors(nc, "water_u", "water_v")
nc_close(nc)


limits <- read_csv("limits.csv")
df <-
  nc_df %>%
  mutate(component = str_sub(component, -1)) %>%
  filter(
    between(lon, limits$west, limits$east),
    between(lat, limits$south, limits$north)
  ) %>%
  spread(component, value) %>%
  arrange(lon, lat) %>%
  mutate(dttm = as_datetime(date))

write_csv(df, here("currents_hycom_2016.csv"))

nc_df %>%
  filter(
    between(lon, limits$west, limits$east),
    between(lat, -30, 30)
  ) %>%
  ggplot() +
  geom_tile(aes(x = lon, y = lat, fill = value)) +
  borders(fill = "green4", alpha = 0.1) +
  coord_quickmap(xlim = c(limits$west, limits$east), ylim = c(-30, 30)) +
  scale_y_continuous(expand = c(0, 0)) + scale_x_continuous(expand = c(0, 0)) +
  facet_wrap(vars(component)) +
  scale_fill_distiller(palette = "RdBu") +
  theme_minimal()
