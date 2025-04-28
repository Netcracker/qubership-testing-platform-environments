FROM path-to-java-image

LABEL maintainer="our-team@qubership.org"
LABEL qubership.atp.service="atp-environments"

ENV HOME_EX=/atp-environments
ENV JDBC_URL=jdbc:postgresql://localhost:5432/envconf
ENV ENVIRONMENT_DB_USER=envconf
ENV ENVIRONMENT_DB_PASSWORD=envconf
ENV REGISTERED_CLIENT=
ENV HOME_LINK=/

WORKDIR $HOME_EX

COPY --chmod=775 dist/atp /atp/
COPY --chown=atp:root build $HOME_EX/

RUN find $HOME_EX -type f -name '*.sh' -exec chmod a+x {} + && \
    find $HOME_EX -type d -exec chmod 777 {} \;

EXPOSE 8080 9000

USER atp

CMD ["./run.sh"]
