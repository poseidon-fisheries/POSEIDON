library(tidyverse)
library(ncdf4)

extract_ncdf_matrix <- function(nc, var, lon_var = "lon", lat_var = "lat") {
  m <- ncvar_get(nc, var)
  quoted_lon_var <- enquo(lon_var)
  quoted_lat_var <- enquo(lat_var)
  if (length(dim(m)) == 3) { m <- m[, , 1] } # surely there is a more elegant way to do this...
  m %>% # get values for depth 1
    `colnames<-`(ncvar_get(nc, lat_var)) %>%
    `rownames<-`(ncvar_get(nc, lon_var)) %>%
    as_tibble(rownames = lon_var) %>%
    gather(!!lat_var, "value", -!!quoted_lon_var, na.rm = TRUE) %>%
    type_convert("ddd") %>%
    # filter missing vals ourself as ncvar_get gets confused if `missing_value` is also filled:
    filter(value != ncatt_get(nc, var, "_FillValue")$value)
}

extract_current_vectors <- function(nc, u_var = "u", v_var = "v") {
  tibble(component = c(u_var, v_var)) %>%
    group_by(component) %>%
    group_map(~ extract_ncdf_matrix(nc, .y$component)) %>%
    ungroup()
}
