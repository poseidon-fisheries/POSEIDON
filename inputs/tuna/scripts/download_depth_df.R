library(tidyverse)
library(raster)
library(httr)

download_depth_df <- function (land_height, left_lon, top_lat, right_lon, bottom_lat, resolution) {
  resp <- GET("https://octopus.zoo.ox.ac.uk/geoserver/wcs", query = list(
    service = "WCS",
    version = "1.0.0",
    request = "GetCoverage",
    format = "geotiff",
    coverage = "context:depth_none_009_gebco",
    bbox = str_glue(left_lon, top_lat, right_lon, bottom_lat, .sep = ","),
    crs = "EPSG:4326",
    resx = resolution,
    resy = resolution
  ))
  tif_file <- tempfile(fileext = ".tif")
  writeBin(content(resp, "raw"), tif_file)
  depth_df <-
    raster(tif_file) %>%
    rasterToPoints() %>%
    as.data.frame() %>%
    rename(depth = 3) %>%
    mutate(depth = ifelse(depth == -99999, land_height, depth)) # -99999 indicates land
  file.remove(tif_file)
  depth_df
}

# To plot a depth_df:
# ggplot(depth_df) + geom_tile(aes(x = x, y = y, fill = depth))
