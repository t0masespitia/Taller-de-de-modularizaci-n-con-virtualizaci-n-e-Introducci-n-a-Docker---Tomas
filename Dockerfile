FROM eclipse-temurin:17-jdk

WORKDIR /usrapp/bin

ENV PORT=6000

COPY target/classes /usrapp/bin/classes

CMD ["java","-cp","./classes","edu.tdse.MicroSpringBoot","edu.tdse.ejemplo.GreetingController"]