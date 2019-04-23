library(rvest)
library(tidyverse)
library(here)

base_url <- "https://www.iattc.org/VesselRegister/"

pages_df <-
  paste0(base_url, "VesselList.aspx?List=AcPS&Lang=ENG") %>%
  read_html() %>%
  html_nodes(".VesselNameLink > a") %>%
  html_attr("href") %>%
  paste0(base_url, .) %>%
  map_df(~ tibble(url = ., html = map(url, read_html)))

df <-
  pages_df %>%
  transmute(
    url = url,
    name = map_chr(html, ~ html_text(html_node(., ".VesselNameTitle"))),
    activity = map_chr(html, ~ html_text(html_node(., ".VesselActivity"))),
    details = map(html, ~ html_node(., "#DetailsTable") %>%
      html_table(fill = TRUE) %>%
      transmute(
        key = if_else(X2 == X3, X1, X2),
        key = str_match(key, "(.*?)(?:$|:)")[, 2], # remove trailing colon
        value = X3
      ))
  ) %>%
  unnest() %>%
  spread(key, value) %>%
  mutate_all(~ ifelse(. == "", NA, .)) %>%
  arrange(`IATTC Vessel Number`)

write_csv(df, here("raw", "vessels_register.csv"))
