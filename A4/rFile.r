df <- read.table(file="C:/Users/hp/Downloads/results.txt")
plot(df$V2, type="l",ylab="Flight Price(in Dollars)", xlab="Week",main="Weekly Median Price",las=1,xaxt="n")