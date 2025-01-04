#scp -v -o "StrictHostKeyChecking=no" -rp . rvk@kube05:nfs/git/zrmiles-frontend
#ssh -o "StrictHostKeyChecking=no" rvk@kube05 rm -rf nfs/git/zrmiles-frontend/target
PATH=/snap/bin:$PATH

mkdir -p /home/rvk/nfs/git/zrmiles-frontend
cp -rp $WORKSPACE/* /home/rvk/nfs/git/zrmiles-frontend
rm -rf /home/rvk/nfs/git/zrmiles-frontend/target
. scripts/settings
export IMAGE_TAG=${NUMBER_OF_COMMITS}-$LAST_HASH

echo -- '------------------------- ENV START -----------------------'
printenv
echo -- '------------------------- ENV END -----------------------'

cd src/main/helm

if helm list | grep zrmiles-frontend-build; then
  helm uninstall --namespace default zrmiles-frontend-build
fi

helm install --namespace default zrmiles-frontend-build build-job
sleep 60

COUNTER=1

while [ $COUNTER -lt 30 ]
do
  sleep 5
  COUNTER=$(expr $COUNTER + 1)

  if [ -f /home/rvk/nfs/git/zrmiles-frontend/target/zrmiles-frontend-1.0.0-SNAPSHOT.jar ]; then
    COUNTER=40
  fi
done

if [ -f /home/rvk/nfs/git/zrmiles-frontend/target/zrmiles-frontend-1.0.0-SNAPSHOT.jar ]; then
  if helm list | grep zrmiles-frontend-kaniko; then
    helm uninstall --namespace default zrmiles-frontend-kaniko
  fi

   envsubst < kaniko-job/values.yaml| helm install --namespace default zrmiles-frontend-kaniko kaniko-job --values -
else
  echo 'Build failed'
  exit 1
fi

sleep 20
COUNTER=1

while [ $COUNTER -lt 30 ]
do
  sleep 5
  COUNTER=$(expr $COUNTER + 1)

  if curl http://skorpion:5000/v2/net.opfietse/zrmiles-frontend/tags/list|jq .|grep $IMAGE_TAG
  then
    COUNTER=40
  fi
done

curl http://skorpion:5000/v2/net.opfietse/zrmiles-frontend/tags/list|jq .|grep $IMAGE_TAG
