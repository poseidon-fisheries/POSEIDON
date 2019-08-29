library(raster)
library(fs)
library(tidyverse)

# This reads all the raster (i.e., `.grd`) files in the `raw/` folder and convert them to CSV
# As of now, we only have BET habitability, but more will come

dir_ls("raw", glob = "*.grd") %>% walk(~ {
  output_file <- path_file(.) %>% path_ext_set("csv")
  raster(.) %>% rasterToPoints() %>% as_tibble() %>% write_csv(output_file)
})
