FROM bellsoft/liberica-openjdk-alpine-musl:21.0.6

LABEL maintainer="our-team@qubership.org"
LABEL qubership.atp.service="atp-environments"

ENV HOME_EX=/atp-environments
ENV JDBC_URL=jdbc:postgresql://localhost:5432/envconf
ENV ENVIRONMENT_DB_USER=envconf
ENV ENVIRONMENT_DB_PASSWORD=envconf
ENV REGISTERED_CLIENT=
ENV HOME_LINK=/

WORKDIR $HOME_EX

RUN mkdir -p dist/atp deployments/update

COPY deployments/install/* deployments/install/

RUN cp -r deployments/install/* deployments/update/ && \
    find deployments -maxdepth 1 -regex '.*/\(install\|update\|atp-common-scripts\)$' -exec mv -t dist/atp {} +

COPY build-context/env-distribution-1.5.85-SNAPSHOT-custom-build.zip /tmp/

RUN unzip /tmp/env-distribution-1.5.85-SNAPSHOT-custom-build.zip -d $HOME_EX/
RUN cp -r dist/atp /atp/ && chmod -R 775 /atp/
RUN chown -R atp:root $HOME_EX/
RUN find $HOME_EX -type f -name '*.sh' -exec chmod a+x {} +
RUN find $HOME_EX -type d -exec chmod 777 {} \;

EXPOSE 8080 9000

USER 1007

CMD ["./run.sh"]
