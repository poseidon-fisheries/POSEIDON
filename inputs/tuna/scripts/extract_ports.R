library(tidyverse) # must be loaded before Hmisc (https://github.com/tidyverse/haven/issues/86#issuecomment-421645194)
library(Hmisc) # `sudo apt install mdbtools` needed for `mdb.get` to work on Ubuntu
library(biogeo)
library(knitr)
library(here)

limits <- read_csv(here("limits.csv"))

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
    `PUERTO SUCRE` = "CUMANA (PUERTO SUCRE)",
    `CUMANA` = "CUMANA (PUERTO SUCRE)",
    `ILO` = "PUERTO ILO"
  )) %>%
  mutate(wpi_port_name = case_when(
    # Reassign some ships as per notes from K.
    # When operating from > 1 ports, took first one in document
    `IATTC Vessel Number` == 14604 ~ "PAGO PAGO HARBOR",
    `IATTC Vessel Number` == 3196 ~ "MAZATLAN",
    `IATTC Vessel Number` == 3694 ~ "MAZATLAN",
    `IATTC Vessel Number` == 14592 ~ "MANTA",
    `IATTC Vessel Number` == 991 ~ "GUAYAQUIL", # for POSORJA
    `IATTC Vessel Number` == 3274 ~ "MANTA",
    `IATTC Vessel Number` == 3682 ~ "MANTA",
    `IATTC Vessel Number` == 3277 ~ "GUAYAQUIL", # for POSORJA
    `IATTC Vessel Number` == 3727 ~ "MANTA",
    `IATTC Vessel Number` == 3232 ~ "MANTA",
    `IATTC Vessel Number` == 4099 ~ "MANTA",
    `IATTC Vessel Number` == 2557 ~ "MANTA",
    `IATTC Vessel Number` == 3577 ~ "MAZATLAN",
    `IATTC Vessel Number` == 3010 ~ "GUAYAQUIL", # for POSORJA
    `IATTC Vessel Number` == 3151 ~ "MANTA",
    `IATTC Vessel Number` == 3616 ~ "BALBOA",
    `IATTC Vessel Number` == 3250 ~ "MANTA",
    `IATTC Vessel Number` == 3451 ~ "GUAYAQUIL",
    `IATTC Vessel Number` == 115 ~ "MANTA",
    `IATTC Vessel Number` == 3928 ~ "MAZATLAN",
    `IATTC Vessel Number` == 13720 ~ "MANTA",
    `IATTC Vessel Number` == 4138 ~ "GUAYAQUIL", # for POSORJA
    `IATTC Vessel Number` == 12466 ~ "BALBOA",
    `IATTC Vessel Number` == 28 ~ "MANTA",
    `IATTC Vessel Number` == 3979 ~ "GUAYAQUIL", # for POSORJA
    `IATTC Vessel Number` == 3259 ~ "BALBOA",
    `IATTC Vessel Number` == 3919 ~ "MANTA",
    `IATTC Vessel Number` == 3943 ~ "MANTA",
    `IATTC Vessel Number` == 4114 ~ "MANTA",
    `IATTC Vessel Number` == 3937 ~ "GUAYAQUIL", # for POSORJA
    TRUE ~ wpi_port_name
  )) %>%
  mutate(link = paste0("[", name, "](", url, ")")) %>%
  select(-reassigned_port_name)

# Unknown ports:
#   Sta. Eugenia de Riveira: in Spain - not in WPI, but outside map anyway
#   Punta De Piedras: in Venezuela - not in WPI, but outside map anyway
vessels_df %>%
  anti_join(ports_df) %>%
  arrange(wpi_port_name, link) %>%
  select(link, `IATTC Vessel Number`, wpi_port_name) %>%
  kable()

vessels_df %>%
  ggplot(aes(`Gross tonnage`)) +
  geom_histogram(color = "grey20", alpha = 0.8) +
  theme_minimal() +
  theme(
    panel.grid.minor.x = element_blank(),
    panel.grid.major.x = element_blank()
  )


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
    between(lat, limits$south, limits$north),
    between(lon, limits$west, limits$east)
  ))

known_ports %>%
  dplyr::select(port_name = wpi_port_name, lon, lat) %>%
  arrange(port_name) %>%
  write_csv(here("ports.csv"))

boats_df <-
  vessels_df %>%
  inner_join(known_ports) %>%
  transmute(
    port_name = wpi_port_name,
    hold_volume_in_m3 = `Fish hold volume (m3)` %>%
      str_match(., "(^\\d+)(?:.*Total capacity: (\\d+)m3)?") %>%
      {
        coalesce(.[, 3], .[, 2])
      } %>%
      as.integer(),
    carrying_capacity_in_t = `Carrying capacity (t)`,
    length_in_m = Length,
    beam_in_m = Beam,
    engine_power_in_hp = `Engine power (HP)`
  )

write_csv(boats_df, here("boats.csv"))

known_ports %>%
  inner_join(ports_df) %>%
  dplyr::select(wpi_port_name, country, num_ships, lon, lat) %>%
  kable()

known_ports %>%
  left_join(ports_df) %>%
  mutate(port = fct_reorder(str_glue("{wpi_port_name} ({country})"), num_ships, .desc = TRUE)) %>%
  ggplot(aes(port, num_ships, fill = country, color = country)) +
  geom_col(alpha = 0.8) +
  theme_minimal() +
  theme(
    panel.grid.major.x = element_blank(),
    axis.text.x = element_text(angle = 45, hjust = 1)
  )

# Ports outside area:
vessels_df %>%
  anti_join(known_ports) %>%
  group_by(wpi_port_name) %>%
  summarise(num_ships = n()) %>%
  inner_join(ports_df) %>%
  dplyr::select(wpi_port_name, country, num_ships, lon, lat) %>%
  arrange(desc(num_ships)) %>%
  kable()

# Download and save a map for the area we're targeting:
source(here("scripts", "download_depth_df.R"))
# A land height of 850 is just enough to keep the "Panama canal" open.
# We also add high points at locations of western ports to make sure we have land
depth_df <-
  known_ports %>%
  filter(lon < -125) %>%
  transmute(x = lon, y = lat, depth = 1000000) %>%
  bind_rows(download_depth_df(850, limits$west, limits$south, limits$east, limits$north, 0.1)) %>%
  write_csv(here("depth.csv"))

