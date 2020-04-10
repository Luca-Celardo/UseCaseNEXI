def dirService 
def app
def pom
def version
def majorVersion

pipeline {
	agent any

	parameters {
        string(name: 'dirService', defaultValue: 'ODSDataMartExtractor', description: 'Service project directory')
		string(name: 'dirServiceConfig', defaultValue: 'ODSDataMartExtractor/src/main/java/com/example/odsdatamartextractor/config/serving', description: 'Service project configuration directory')
		string(name: 'namespaceService', defaultValue: 'default', description: 'Namespace')
        string(name: 'dockerRegistry', defaultValue: 'tcp://192.168.99.121:2376', description: 'Docker registry')
		string(name: 'dockerHubUser', defaultValue: 'lucacelardo', description: 'Docker Hub username')
		password(name: 'dockerHubPass', defaultValue: 'secret', description: 'Docker Hub password')
		string(name: 'dockerHubRepo', defaultValue: 'odsdatamartextractor', description: 'Docker Hub repository name of the project')
		string(name: 'containerPort', defaultValue: '8080', description: 'Container service port')
		booleanParam(name: 'deployApplication', defaultValue: false, description: 'True if you want to deploy the application')
		booleanParam(name: 'deployConfigMap', defaultValue: false, description: 'True only if you want to update ConfigMap')
		booleanParam(name: 'deployService', defaultValue: false, description: 'True only for the First Deploy')
		booleanParam(name: 'deployIngress', defaultValue: false, description: 'True only for the First Deploy')
	}

	environment {
		kubeEndpoint = "-:-"
		kubeUser = "-"
	}


	stages {
		stage('Source Checkout') {
			steps {
				script {
					dirService = "${params.dirService}";
					git branch: "master", url: "https://github.com/Luca-Celardo/UseCaseNEXI.git"
				}
			}
		}
		stage('Build Application') {
			steps {
				script {
					dir("${dirService}") {
						bat "mvn -B clean install -DskipTests=true"
						pom = readMavenPom file: 'pom.xml'
						version = pom.version
                    	majorVersion = version.tokenize('.')[0]
					}
				}
			}
		}
		stage('Archive Application') {
			steps {
				script {
					dir("${dirService}") {
						bat "mvn -B package -DskipTests=true"
					}
				}
			}
		}
		stage('Build Image') {
			steps {
				script {
					dir("${dirService}") {
						//bat "docker build -t ${params.dockerHubUser}/odsdatamartextractor:${pom.version} -f Dockerfile ."
						//bat "docker login -u ${params.dockerHubUser} -p ${params.dockerHubPass} docker.io"
						//bat "docker push lucacelardo/odsdatamartextractor:${pom.version}"
					}
				}
			}
		}
		stage ('Parameters Injection') {
			steps {
				script {
					if(params.deployConfigMap) {
						echo "Inject parameter for ConfigMap"
						sh "sed -i 's/<NAMESPACE>/${params.namespaceService}/'  kube/deployment-config.yml"
						sh "sed -i 's/<ARTIFACT_ID>/${pom.artifactId}/'  kube/deployment-config.yml"
					}
					if(params.deployApplication) {
						echo "Inject parameter for Application"
						sh "sed -i 's/<NAMESPACE>/${params.namespaceService}/'  kube/deployment-application.yml"
						sh "sed -i 's/<ARTIFACT_ID>/${pom.artifactId}/'  kube/deployment-application.yml"
						sh "sed -i 's/<MAJOR_VERSION>/${majorVersion}/'  kube/deployment-application.yml"
						sh "sed -i 's/<DOCKER_REGISTRY>/${params.dockerRegistry}/'  kube/deployment-application.yml"
						sh "sed -i 's/<CONTAINER_PORT>/${params.containerPort}/'  kube/deployment-application.yml"
						sh "sed -i 's/<VERSION>/${pom.version}/'  kube/deployment-application.yml"
					}
					if(params.deployService) {
						dir("${dirServiceConfig}") {
							echo "Inject parameter for Service"
							bat '@powershell -Command "get-content ODSDataMartExtractor-service.yaml | %{$_ -replace \'<NAMESPACE>\',\'@{params.namespaceService}\'}"'
							//bat "sed -i 's/<NAMESPACE>/${params.namespaceService}/'  kube/deployment-service.yml"
							//bat "sed -i 's/<DOCKER_HUB_USER>/${params.dockerHubUser}/'  kube/deployment-service.yml"
							//bat "sed -i 's/<DOCKER_HUB_REPOSITORY>/${params.dockerHubRepo}/'  kube/deployment-service.yml"
							//bat "sed -i 's/<MAJOR_VERSION>/${majorVersion}/'  kube/deployment-service.yml"
							//bat "sed -i 's/<CONTAINER_PORT>/${params.containerPort}/'  kube/deployment-service.yml"
						}
					}
					if(params.deployIngress) {
						echo "Inject parameter for Ingress"
						sh "sed -i 's/<NAMESPACE>/${params.namespaceService}/'  kube/deployment-ingress.yml"
						sh "sed -i 's/<ARTIFACT_ID>/${pom.artifactId}/'  kube/deployment-ingress.yml"
						sh "sed -i 's/<MAJOR_VERSION>/${majorVersion}/'  kube/deployment-ingress.yml"
						sh "sed -i 's|<CONTEXT_ROOT>|${params.contextRoot}|'  kube/deployment-ingress.yml"
						sh "sed -i 's/<CONTAINER_PORT>/${params.containerPort}/'  kube/deployment-ingress.yml"
					}
				}
			}
		}
		stage('Kubernetes - Deploy') {
			steps {
				script {
					//withKubeConfig(caCertificate: '', credentialsId: kubeUser, serverUrl: kubeEndpoint) {
						if(params.deployConfigMap) {
							sh "kubectl apply -f kube/deployment-config.yml"
						}
					
						if(params.deployApplication) {
							sh "kubectl apply -f kube/deployment-application.yml"
						}
						
						if(params.deployService) {
							//sh "kubectl apply -f kube/deployment-service.yml"
						}
						
						if(params.deployIngress) {
							sh "kubectl apply -f kube/deployment-ingress.yml"
						}
					//}
				}
			}
		}
	}
}