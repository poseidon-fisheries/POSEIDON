library(rgdal)
library(maptools)




#read the raster
library(rgdal)
library(RColorBrewer)
library(raster)

path<-"/home/carrknight/Downloads/wc1000/wc1000/w001001.adf"

x <- new("GDALReadOnlyDataset", path)
getDriver(x)
getDriverLongName(getDriver(x))
xx<-asSGDF_GROD(x)
r<-raster(xx)
plot(r)
#read the mpa
data.shape<-readOGR(dsn="/home/carrknight/code/oxfish/src/main/resources/cssr_mpa/",layer="CCSR_MarineProtectedAreas")
plot(data.shape)
plot(data.shape,add=TRUE) #can't see it because
#the projections are wrong
proj4string(xx)
proj4string(data.shape)
#translate
data.shape<-spTransform(data.shape,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
#replot
plot(r)
plot(data.shape,add=TRUE)





#save
writeOGR(data.shape,dsn = "/home/carrknight/code/oxfish/src/main/resources/cssr_mpa/reprojected/",layer = "mpa_central",driver="ESRI Shapefile")

#modify the others too
data.shape<-readOGR(dsn="/home/carrknight/code/oxfish/src/main/resources/ncssr_mpa/",layer="MPA_NCCSR_AdoptedMPAs")
data.shape<-spTransform(data.shape,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
plot(r)
plot(data.shape,add=TRUE)
writeOGR(data.shape,dsn = "/home/carrknight/code/oxfish/src/main/resources/ncssr_mpa/reprojected/",layer = "mpa_north",driver="ESRI Shapefile")

data.shape<-readOGR(dsn="/home/carrknight/code/oxfish/src/main/resources/ncssr_mpa/",layer="MPA_NCCSR_AdoptedMPAs")
data.shape<-spTransform(data.shape,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
plot(r)
plot(data.shape,add=TRUE)
writeOGR(data.shape,dsn = "/home/carrknight/code/oxfish/src/main/resources/ncssr_mpa/reprojected/",layer = "mpa_north",driver="ESRI Shapefile")




#cities
data.shape2<-readOGR(dsn="/home/carrknight/Downloads/tmp/major/",layer="Major_US_Cities")
data.shape2<-spTransform(data.shape2,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
#clip the cities
data.shape2<-crop(data.shape2,extent(r))
plot(data.shape2,add=TRUE)

#CITIES
plot(r)
plot(data.shape,add=TRUE)


#cities
data.shape2<-readOGR(dsn="/home/carrknight/Downloads/tmp/major/",layer="Major_US_Cities")
data.shape2<-spTransform(data.shape2,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
#clip the cities
data.shape2<-crop(data.shape2,extent(r))
plot(r)
plot(data.shape,add=TRUE)
plot(data.shape2,add=TRUE)
writeOGR(data.shape2,dsn = "/home/carrknight/code/oxfish/src/main/resources/cities/",layer = "cities",driver="ESRI Shapefile")


#sablefish
sable<-readOGR(dsn="/home/carrknight/Downloads/tmp/sablefish/",layer="A_fimbria2006")
sable<-spTransform(sable,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
plot(r)
plot(data.shape,add=TRUE)
plot(data.shape2,add=TRUE)
plot(sable,add=TRUE)
head(sable@data)


rr<-readGDAL(  "/home/carrknight/Dropbox/OC Systems-Based Fisheries Management/ABM1 development/Parameters & logic/Spatial Ecological Data/shp/NCCOS Darkblotched rockfish Abundance1.tif")
proj4string(rr)
rr<-spTransform(rr,CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))
tiff<-raster(rr)
plot(tiff)

#wgs 1984 utm zome 10n
# ellipses: wgs 1984

library(gdalUtils)
gdalwarp(srcfile = "/home/carrknight/gdrive/forErnesto/Pacific EFH data/Modeled Species/NCCOS Dover sole Abundance1.tif",
         dstfile= "/home/carrknight/sole.tif"
         ,t_srs="+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0",
         verbose=TRUE,output_Raster=TRUE)
src_dataset<-readGDAL("/home/carrknight/sole.tif")
proj4string(src_dataset)
plot(raster(src_dataset))
rasterizedFish<-raster(src_dataset)
writeRaster(rasterizedFish, filename="/home/carrknight/soletest.asc", format = "ascii", datatype='FLT4S', overwrite=TRUE) 


plot(rasterizedFish)

#check the position of the MPAs relative to the fishing 