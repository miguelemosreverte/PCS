aws ec2 create-key-pair \
--key-name MyKeyPair \
--query 'KeyMaterial' \
--output text > MyKeyPair.pem

aws ec2 describe-key-pairs \
--key-name MyKeyPair

chmod 400 ./MyKeyPair.pem
ssh-keygen -y -f ./MyKeyPair.pem > my-public-key.pub


eksctl create cluster \
--name prod  \
--version 1.17  \
--region us-west-2  \
--nodegroup-name linux-nodes  \
--node-type t2.medium  \
--nodes 2  \
--nodes-min 1  \
--nodes-max 4  \
--ssh-access  \
--ssh-public-key my-public-key.pub  \
--managed

rm my-public-key.pub

kubectl apply -f admin