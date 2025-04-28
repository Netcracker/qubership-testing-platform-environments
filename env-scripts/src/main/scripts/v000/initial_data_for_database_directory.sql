/*Types*/
INSERT INTO public.database_directory
("name", url_format)
VALUES('oracle', 'jdbc:oracle:thin:@host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('postgresql', 'jdbc:postgresql://host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('cassandra', 'jdbc:cassandra://host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('mysql', 'jdbc:mysql://host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('mongo', 'jdbc:mongo://host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('hive2', 'jdbc:hive2://host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('arango', 'jdbc:arango://host:port/name');
INSERT INTO public.database_directory
("name", url_format)
VALUES('bigquery', 'jdbc:BQDriver:name');
