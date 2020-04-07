@Library('k8s-mgmt-library')_

def dirProject 
def app
def pom
def majorVersion

pipeline {
	agent any

	parameters {
        string(name: 'dirProject', defaultValue: 'ODSDataMartExtractor', description: 'Cartella effettiva del progetto')
        string(name: 'dockerRegistry', defaultValue: 'tcp://192.168.99.121:2376', description: 'Docker registry')
		string(name: 'namespaceService', defaultValue: 'bu-arc', description: 'Namespace')
		string(name: 'containerPort', defaultValue: '8080', description: 'Container service port')
		string(name: 'contextRoot', defaultValue: '/nexi-extractor', description: 'Application context root')
		booleanParam(name: 'deployApplication', defaultValue: true, description: 'True if you want to deploy the application')
		//booleanParam(name: 'deployConfigMap', defaultValue: false, description: 'True only if you want to update ConfigMap')
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
					dirProject = "${params.dirProject}";
					git branch: "master", credentialsId: "GitTecnicalUser", url: "https://github.com/Luca-Celardo/UseCaseNEXI.git"
				}
			}
		}
		/*stage ('Set environment variables'){
			steps {
				script {
					kubeEndpoint = utils.getKubeEndpoint('k8s-devops')
					kubeUser = utils.getKubeNamespaceCredentials('k8s-devops') + '-' + params.namespaceService
				}
			}
		}*/
		stage('Build Application') {
			steps {
				script {
					dir("${dirProject}") {
						sh "mvn -B clean install -DskipTests=true"
						pom = readMavenPom file: 'pom.xml'
						def version = pom.version
                    	majorVersion = version.tokenize('.')[0]
					}
				}
			}
		}
		stage('Archive Application') {
			steps {
				script {
					dir("${dirProject}") {
						sh "mvn -B package -DskipTests=true"
					}
				}
			}
		}
		stage('Build Image') {
			steps {
				script {
					dir("${dirProject}") {
				  		/*app = docker.build("${params.dockerRegistry}/${pom.artifactId}:${pom.version}", "--build-arg JAR_FILE=target/${pom.artifactId}-${pom.version}.jar .")
						docker.withRegistry("http://${params.dockerRegistry}", "dockerRegistryUser"){
							app.push()
						}
						sh "docker rmi ${params.dockerRegistry}/${pom.artifactId}:${pom.version}"
					}
				}
			}
		}
		/*stage ('Parameters Injection') {
			steps {
				script {
					/*if(params.deployConfigMap) {
						echo "Injcet parameter for ConfigMap"
						sh "sed -i 's/<NAMESPACE>/${params.namespaceService}/'  kube/deployment-config.yml"
						sh "sed -i 's/<ARTIFACT_ID>/${pom.artifactId}/'  kube/deployment-config.yml"
					}*/
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
						echo "Inject parameter for Service"
						sh "sed -i 's/<NAMESPACE>/${params.namespaceService}/'  kube/deployment-service.yml"
						sh "sed -i 's/<ARTIFACT_ID>/${pom.artifactId}/'  kube/deployment-service.yml"
						sh "sed -i 's/<MAJOR_VERSION>/${majorVersion}/'  kube/deployment-service.yml"
						sh "sed -i 's/<CONTAINER_PORT>/${params.containerPort}/'  kube/deployment-service.yml"
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
		}*/
		stage('Kubernetes - Deploy') {
			steps {
				script {
					withKubeConfig(caCertificate: '', credentialsId: kubeUser, serverUrl: kubeEndpoint) {
						/*if(params.deployConfigMap) {
							sh "kubectl apply -f kube/deployment-config.yml"
						}*/
					
						if(params.deployApplication) {
							sh "kubectl apply -f kube/deployment-application.yml"
						}
						
						if(params.deployService) {
							sh "kubectl apply -f kube/deployment-service.yml"
						}
						
						if(params.deployIngress) {
							sh "kubectl apply -f kube/deployment-ingress.yml"
						}
					}
				}
			}
		}*/
	}
}
