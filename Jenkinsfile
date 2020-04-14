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
		string(name: 'containerPort', defaultValue: '8080', description: 'Container service port')
		string(name: 'kubeconfigPath', defaultValue: 'C:/Users/Luca/.kube/config', description: 'Kubeconfig file path')
		string(name: 'gitHubUrl', defaultValue: 'https://github.com/Luca-Celardo/UseCaseNEXI.git', description: 'GitHub repository project URL')
		string(name: 'dockerHubUser', defaultValue: 'lucacelardo', description: 'Docker Hub username')
		password(name: 'dockerHubPass', defaultValue: 'secret', description: 'Docker Hub password')
		string(name: 'dockerHubRepo', defaultValue: 'odsdatamartextractor', description: 'Docker Hub repository name of the project')
		booleanParam(name: 'deployApplication', defaultValue: false, description: 'True if you want to deploy the application')
		booleanParam(name: 'deployService', defaultValue: true, description: 'True if you want to deploy the service')
	}

	stages {
		stage('Source Checkout') {
			steps {
				script {
					dirService = "${params.dirService}";
					git branch: "master", url: "${params.gitHubUrl}"
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
						bat "docker build -t ${params.dockerHubUser}/${params.dockerHubRepo}:${pom.version} -f Dockerfile ."
						bat "docker login -u ${params.dockerHubUser} -p ${params.dockerHubPass} docker.io"
						bat "docker push ${params.dockerHubUser}/${params.dockerHubRepo}:${pom.version}"
					}
				}
			}
		}
		stage('Kubernetes - Deploy') {
			steps {
				script {
					if(params.deployApplication) {
						dir("./configurations") {
							bat "kubectl apply -f cronjob-source.yaml --kubeconfig ${params.kubeconfigPath}"
							bat "kubectl apply -f kafka-datamartextractor-source-topic.yaml --kubeconfig ${params.kubeconfigPath}"
							bat "kubectl apply -f kafka-datamartextractor-topic.yaml --kubeconfig ${params.kubeconfigPath}"
							bat "kubectl apply -f kafka-dataconverter-source-topic.yaml --kubeconfig ${params.kubeconfigPath}"
							bat "kubectl apply -f kafka-dataconverter-topic.yaml --kubeconfig ${params.kubeconfigPath}"
							bat "kubectl apply -f kafka-filemanager-source-topic.yaml --kubeconfig ${params.kubeconfigPath}"
							bat "kubectl apply -f kafka-filemanager-topic.yaml --kubeconfig ${params.kubeconfigPath}"
						}
					}
						
					if(params.deployService) {
						dir("${dirServiceConfig}") {
							bat "kubectl apply -f ${params.dirService}-service.yaml --kubeconfig ${params.kubeconfigPath}"
						}
					}
				}
			}
		}
	}
}