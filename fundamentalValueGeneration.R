nbRounds  = 1000
nbRepetitions = 1000
fundamentalValue = matrix(0, nrow=nbRepetitions, ncol=nbRounds)
for(j in 1:nbRepetitions){
fundamentalValue[j,1]=44
for(i in 2:nbRounds)
	fundamentalValue[j,i] = round(fundamentalValue[j,i-1]+runif(1, min=-0.5, max=0.5),2)

}
write.table(fundamentalValue, file="fundamentalValue", sep="\t", col.names=FALSE, row.names=FALSE)



