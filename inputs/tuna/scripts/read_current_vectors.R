library(tidyverse)
library(ncdf4)
library(here)

file <- here("raw", "EasternPacific_interim_historic_1x30d_19790315_PHYS.nc")
nc <- nc_open(file)
lon <- ncvar_get(nc, "lon") - 360
lat <- ncvar_get(nc, "lat")

extract_to_df <- function(varname) {
  cube <- ncvar_get(nc, varname)
  fill_value <- ncatt_get(nc, varname, "_FillValue")
  cube[cube == fill_value$value] <- NA
  m <- cube[, , 1]
  image(lon, lat, m)

  colnames(m) <- lat
  rownames(m) <- lon
  as_tibble(m, rownames = "lon") %>%
    gather(key = "lat", value = !!varname, -lon) %>%
    type_convert()
}

df <-
  extract_to_df("u") %>%
  inner_join(extract_to_df("v")) %>%
  drop_na() %>%
  arrange(lon, lat) %>%
  write_csv(here("currents.csv"))

df %>%
  gather(key = "component", value = value, u, v) %>%
  filter(abs(lat) <= 20) %>%
  filter(lon >= -150) %>%
  ggplot() +
  geom_tile(aes(x = lon, y = lat, fill = value)) +
  coord_fixed() +
  facet_wrap(vars(component)) +
  scale_fill_distiller(palette = "RdBu") +
  theme_minimal()
