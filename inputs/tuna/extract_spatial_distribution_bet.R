library(tidyverse)
library(ncdf4)
library(here)

source(here("scripts", "extract_ncdf_data.R"))

nc <- nc_open(here("raw", "EasternPacific_interim_historic_1x30d_BET_20101215.nc"))

df <- extract_ncdf_matrix(nc, "bet_tot")

df %>%
  filter(between(lat, -20, 20)) %>%
  ggplot() +
  geom_tile(aes(x = lon, y = lat, fill = value)) +
  coord_fixed() +
  scale_fill_distiller(palette = "RdBu") +
  theme_minimal()

