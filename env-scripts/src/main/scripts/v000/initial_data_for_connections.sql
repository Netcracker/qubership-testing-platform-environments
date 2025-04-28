INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('0a91bc83-6d09-4a5e-a935-7713e82ea2c1'::uuid, 'File over FTP', 'File over FTP', 'ITF40', '{"host":"","path":"","principal":"","credentials":"","destinationFileName":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('0a90bb83-6f09-4c4b-a925-9763e86ea8c2'::uuid, 'File over SFTP', 'File over SFTP', 'ITF40', '{"host":"","path":"","principal":"","credentials":"","destinationFileName":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('adf11d14-ffab-4e13-bff0-eb8afc9925b5'::uuid, 'File over SMB', 'File over SMB', 'ITF40', '{"host":"","path":"","principal":"","credentials":"","destinationFileName":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('de31a1a7-630d-4fa2-8838-aa8473d2fff3'::uuid, 'JMS Asynchronous', 'JMS Asynchronous', 'ITF40', '{"destinationType":"","destination":"","connectionFactory":"","credentials":"","principal":"","initialContextFactory":"","addJndiProps":"","authentication":"","providerUrl":"","messageSelector":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('d58679cd-3616-4a26-b2e3-721638c535ff'::uuid, 'REST over HTTP', 'REST over HTTP', 'ITF40', '{"endpointUri":"","allowStatus":"","headers":"","credentials":"","principal":"","method":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('355139dd-2863-f272-0413-47fe5bc1e233'::uuid, 'REST Synchronous', 'REST Synchronous', 'ITF40', '{"endpoint":"","headers":"","contentType":"","responseCode":"","baseUrl":"","method":"","secureProtocol":"","properties":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('4e533a90-ec7e-4244-bd5f-285c766f066e'::uuid, 'SOAP Over HTTP Synchronous', 'SOAP Over HTTP Synchronous', 'ITF40', '{"endpoint":"","headers":"","contentType":"","wsdlPath":"","isWsdlContains":"","requestXSDPath":"","responseXSDPath":"","responseCode":"","method":"","baseUrl":"","secureProtocol":"","properties":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('7c7f16b6-796c-43ef-b610-f335b703c489'::uuid, 'SOAP Over JMS', 'SOAP Over JMS', 'ITF40', '{"providerUrl":"","initialContextFactory":"","principal":"","credentials":"","authentication":"","destinationType":"","outConnectionFactory":"","outDestination":"","inConnectionFactory":"","inDestination":"","addJndiProps":"","wsdlPath":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('174841ce-a996-3659-ae03-ecc20b6110a2'::uuid, 'SS7 Transport', 'SS7 Transport', 'ITF40', '{"portTango":"","hostnameTango":"","isProxy":"","portApp":"","hostnameApp":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('7f85186a-05ed-4c18-bfcc-89c30fa58972'::uuid, 'Diameter Synchronous', 'Diameter Synchronous', 'ITF40', '{"host":"","port":"","dwa":"","configPath":"","waitResponseTimeout":"","connectionType":"","interceptorName":"","properties":"","CER":"","messageFormat":"","waitResponse":"","direction":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('376176f9-cc18-4d12-a90c-c13d132307f4'::uuid, 'DDRS', NULL, 'service', '{"key":"","host":"","login":"","password":"","command":"","fileMask":"","searchExpression":"","sourceDir":"","timeout":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('2a0eab16-0fe7-4a12-8155-78c0c151abdf'::uuid, 'HTTP', NULL, 'service', '{"url":"","login":"","password":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('e4b8a9aa-1952-4be7-8687-56d95f078d32'::uuid, 'HTTP-OpenShiftProject', NULL, 'service', '{"url": "", "login": "", "token": "", "project": "", "password": "", "etalon_project": ""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('657ecd97-b08c-46dd-bd87-6f574429c468'::uuid, 'HTTP-KubernetesProject', NULL, 'service', '{"url":"","login":"","password":"","namespace":"","token":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('2cb3b9e0-0067-46af-8f18-b103fbc19a73'::uuid, 'HTTP-OpenShiftRout', NULL, 'service', '{"url": "", "login": "", "password": "", "route_name": "", "root_synchronize_project": ""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('24136d83-5ffb-487f-9bb4-e73be3a89aa2'::uuid, 'SSH', NULL, 'service', '{"ssh_host":"","ssh_login":"","ssh_password":"","ssh_key":"","passphrase":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('2c01eb66-63be-4484-9558-f374fe72f519'::uuid, 'LDAP', NULL, 'service', '{"host":"","port":"","login":"","password":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('a03d3884-36d5-40ad-947a-7e2c3f0febcb'::uuid, 'TA Engines Provider', NULL, 'service', '{"Provider_URL": "http://atp-dealer:8080", "Release_Tool_Path": "/${$.id}", "Acquire_Content_Type": "application/json", "Provider_Engine_Type": "NTT", "Acquire_Create_Tool_Path": "/ntt ", "Release_Tool_HTTP_Method": "Delete", "Engine_Service_URL_JSON_Path": "$.url", "Acquire_Create_Tool_HTTP_Method": "Post", "Acquire_Create_Tool_Request_Body": ""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('3e074b4e-5224-4f63-bea4-819c2ebf0b74'::uuid, 'GIT', NULL, 'service', '{"url":"","token":"","username":"","password":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '[]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('46ca25d6-058e-471a-9b5e-c13e4b481227'::uuid, 'DB', NULL, 'service', '{"db_host": "", "db_name": "", "db_port": "", "db_type": "", "db_login": "", "jdbc_url": "", "db_password": "", "db_properties": ""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, '["HealthCheck","LogCollector","MIA","TDM"]', NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('ddbf674b-728b-4d8b-95e7-a0e55a7c0d70'::uuid, 'HTTP-Consul', NULL, 'service', '{"url":"","token":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, NULL, NULL);
INSERT INTO public.connections
(id, "name", description, tag_list, parameters, connection_type, created_by, created, modified_by, modified, system_id, source_template_id, services, source_id)
VALUES('7690f4e8-62d1-49ec-b2dc-207a2c57b416'::uuid, 'HTTP-CIP', NULL, 'service', '{"url":"","dns":"","public_gateway_url":"","private_gateway_url":"","login":"","password":"","client_id":""}', NULL, NULL, CURRENT_TIMESTAMP, NULL, NULL, '3bdef735-b163-4992-980b-b5e5ebcb77af'::uuid, NULL, NULL, NULL);