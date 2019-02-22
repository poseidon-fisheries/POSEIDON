library(tidyverse)
library(ncdf4)
library(here)

source(here("scripts", "extract_ncdf_data.R"))

nc <- nc_open(here("raw", "EasternPacific_interim_historic_1x30d_19790315_PHYS.nc"))
df <- extract_current_vectors(nc) %>% mutate(lon = lon - 360)
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
