library(rgdal)
library(raster)
# Create a raster
ras <- raster(resolution=c(5,5))
ras[] <- seq.int(from=1,to=ncell(ras))
# Create a SpatialPolygons object
p1 <- rbind(c(-50,-50), c(50,-50), c(50,50), c(-50,50))
shpPolys <- SpatialPolygons( list(Polygons(list(Polygon(p1)), 1)))

# Plot them, one layer after another
plot(ras)
plot(shpPolys, col="yellow", add=TRUE)

shpPolys<- SpatialPolygonsDataFrame(shpPolys,data=as.data.frame("fake"))
writeRaster(ras,filename="/home/carrknight/code/oxfish/src/test/resources/test.asc",format="ascii")
writeOGR(shpPolys,dsn = "/home/carrknight/code/oxfish/src/test/resources/",layer = "fakempa",driver="ESRI Shapefile")


path<-"/home/carrknight/code/oxfish/src/test/resources/test.asc"

x <- new("GDALReadOnlyDataset", path)
getDriver(x)
getDriverLongName(getDriver(x))
xx<-asSGDF_GROD(x)
r <- raster(xx)
plot(r)
plot(ras)
r
ras
ras[19,]
