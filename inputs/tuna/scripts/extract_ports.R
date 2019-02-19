library(tidyverse) # must be loaded before Hmisc (https://github.com/tidyverse/haven/issues/86#issuecomment-421645194)
library(Hmisc) # `apt install mdbtools` needed for `mdb.get` to work
library(stringdist)
library(biogeo)
library(knitr)
library(here)

left_lon <- -180
right_lon <- -60
top_lat <- -50
bottom_lat <- 50

# "WPI.mdb" is the World Port Index, obtained from:
# https://msi.nga.mil/NGAPortal/MSI.portal?_nfpb=true&_pageLabel=msi_portal_page_62&pubCode=0015
ports_df <-
  mdb.get(here("raw", "WPI.mdb"), tables = "Wpi Data") %>%
  transmute(
    country = Wpi.country.code,
    wpi_port_name = as.character(Main.port.name),
    lon = as.numeric(dms2dd(Longitude.degrees, Longitude.minutes, 0, Longitude.hemisphere)),
    lat = as.numeric(dms2dd(Latitude.degrees, Latitude.minutes, 0, Latitude.hemisphere))
  ) %>%
  filter(
    # remove potentially confusing duplicates
    !(wpi_port_name == "MANZANILLO" & country == "CU"),
    !(wpi_port_name == "SAN PEDRO" & country == "CI")
  )

vessels_df <-
  read_csv(here("raw", "vessels_register.csv")) %>%
  mutate(reassigned_port_name = recode(`Port of registration`,
    `Huacho` = "Callao",
    `Vacamonte` = "Panama",
    `Gig Harbor` = "Seattle",
    `Long Beach` = "Los Angeles",
    `Santa Barbara` = "Los Angeles",
    `Ventura` = "Los Angeles"
  )) %>%
  mutate(wpi_port_name = recode(str_to_upper(reassigned_port_name),
    `CALLAO` = "PUERTO DEL CALLAO",
    `CHIMBOTE` = "PUERTO DE CHIMBOTE",
    `HUACHO` = "PUERTO DE HUACHO",
    `MONTEREY (USA)` = "MONTEREY",
    `PAGO PAGO` = "PAGO PAGO HARBOR",
    `PANAMA` = "BALBOA",
    `PUERTO CHIAPAS` = "PUERTO MADERO",
    `PUERTO SUCRE` = "CUMANA (PUERTO SUCRE)"
  )) %>%
  mutate(link = paste0("[", name, "](", url, ")")) %>%
  select(-reassigned_port_name)

# show ports occupying same lon/lat cells
vessels_df %>%
  group_by(`Port of registration`, wpi_port_name) %>%
  summarise(num_ships = n()) %>%
  ungroup() %>%
  inner_join(ports_df) %>%
  group_by(trunc(lon), trunc(lat)) %>%
  nest() %>%
  rowwise() %>%
  filter(nrow(data) > 1) %>%
  unnest() %>%
  filter(!str_detect(wpi_port_name, str_to_upper(`Port of registration`))) %>%
  arrange(wpi_port_name, desc(num_ships)) %>%
  select(`Port of registration`, `Reassigned to` = wpi_port_name, `N. of ships` = num_ships) %>%
  kable()

known_ports <-
  vessels_df %>%
  group_by(wpi_port_name) %>%
  summarise(num_ships = n()) %>%
  ungroup() %>%
  inner_join(filter(
    ports_df,
    # keep only ports inside area of interest
    between(lat, top_lat, bottom_lat),
    between(lon, left_lon, right_lon)
  )) %>%
  dplyr::select(wpi_port_name, num_ships, lon, lat) %>%
  arrange(desc(num_ships)) %>%
  write_csv(here("ports.csv"))

vessels_df %>%
  inner_join(known_ports) %>%
  select(wpi_port_name, `Fish hold volume (m3)`, Length, Beam, `Engine power (HP)`, `Gross tonnage`) %>%
  write_csv(here("boats.csv"))

known_ports %>%
  inner_join(ports_df) %>%
  dplyr::select(wpi_port_name, country, num_ships, lon, lat) %>%
  kable()

# Ports outside area:
vessels_df %>%
  anti_join(known_ports) %>%
  group_by(wpi_port_name) %>%
  summarise(num_ships = n()) %>%
  inner_join(ports_df) %>%
  dplyr::select(wpi_port_name, country, num_ships, lon, lat) %>%
  arrange(desc(num_ships)) %>%
  kable()

# Unknown ports:
#   Sta. Eugenia de Riveira: in Spain - couldn't find in WPI
#   Las Vegas: WTF
#   Ilo: ???
#   Mexico (North): WTF
#   Punta De Piedras: in Venezuela - couldn't find in WPI
vessels_df %>%
  anti_join(ports_df) %>%
  arrange(wpi_port_name, link) %>%
  select(link, wpi_port_name) %>%
  kable()

# Download and save a map for the area we're targeting:
source(here("scripts", "download_depth_df.R"))
# A land height of 850 is just enough to keep the "Panama canal" open.
# We also add high points at locations of western ports to make sure we have land
depth_df <-
  known_ports %>%
  filter(lon < -125) %>%
  transmute(x = lon, y = lat, depth = 1000000) %>%
  bind_rows(download_depth_df(850, left_lon, top_lat, right_lon, bottom_lat, 0.1)) %>%
  write_csv(here("antigua_convention_area.csv"))
