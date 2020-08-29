


eksctl create cluster \
--name prod11  \
--version 1.17  \
--region us-west-2  \
--nodegroup-name linux-nodes  \
--node-type t2.large  \
--nodes 5  \
--nodes-min 5  \
--nodes-max 5  \
--ssh-access  \
--ssh-public-key my-public-key.pub  \
--managed

kubectl apply -f assets/k8s/namespace.yml
kubectl config set-context --current --namespace=copernico
kubectl apply -f assets/aws/admin
