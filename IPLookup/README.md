# IPLookup

## Instruções de uso

Implementar a função no Google Cloud:

gcloud functions deploy funcIPLookup
--project=cn2425-t1-g06
--entry-point=pt.isel.cn.landmarks.iplookup.IPLookup
--runtime=java21
--trigger-http --gen2 
--region=europe-west1
--source=target/deployment

Guardar a URL retornada.

## Utilização 

A função pode ser chamada através de um pedido HTTP GET para a URL obtida no passo anterior, com o seguinte formato:

curl "https://(URL)/iplookup?zone=(Zona GCP)&groupName=(Instance Group)"

zone: zona do Google Cloud onde se encontra a instância

groupName: nome do grupo de instâncias


```