

package StartApplication

object Configurations {
  
  def setBasePath(newBasePath: String) = {
    this.basePath = newBasePath
    println("basepath set to: "+basePath)
    
  modelDataPath = basePath+"/data/westernEuropeWeather.json"
  trainedModelPath = basePath+"/results/model"
  
  learningDataPath = basePath+"/data/westernEuropeWeather0.json"
  offlineAnalysisResults = basePath+"/results/offline/"
  
  checkpointingPath = basePath+"/checkpoint"
  streamingSourcePath = basePath+"/data/*"
  onlineAnalysisDePath = basePath+"/results/collection/onlineAnalysisResults/DE/"
  onlineAnalysisNlPath = basePath+"/results/collection/onlineAnalysisResults/NL/"
  onlineAnalysisChPath = basePath+"/results/collection/onlineAnalysisResults/CH/"
  onlineAnalysisAtPath = basePath+"/results/collection/onlineAnalysisResults/AT/"
  onlineAnalysisFrPath = basePath+"/results/collection/onlineAnalysisResults/FR/"
  onlineAnalysisBePath = basePath+"/results/collection/onlineAnalysisResults/BE/"
  onlineAnalysisLuPath = basePath+"/results/collection/onlineAnalysisResults/LU/"
  onlineAnalysisResultsPath = basePath+"/results/collection/onlineAnalysisResults/"
  comparisonResultsPath = basePath+"/results/collection/comparisonResults/"
  generalDataPath = basePath+"/results/collection/generalData/"
  predictionResultsPath = basePath+"/results/collection/predictionResults/"
  
  }
   
  def setLearningDataPath(newLearningDataPath: String){
    this.learningDataPath = newLearningDataPath
  }
  
  var basePath = "/Users/localuser/Documents/flight"
  
  var modelDataPath = basePath+"/data/westernEuropeWeather.json"
  var trainedModelPath = basePath+"/results/model"
  
  var learningDataPath = basePath+"/data/westernEuropeWeather0.json"
  var offlineAnalysisResults = basePath+"/results/offline/"
  
  var checkpointingPath = basePath+"/checkpoint"
  var streamingSourcePath = basePath+"/data/*"
  var onlineAnalysisDePath = basePath+"/results/collection/onlineAnalysisResults/DE/"
  var onlineAnalysisNlPath = basePath+"/results/collection/onlineAnalysisResults/NL/"
  var onlineAnalysisChPath = basePath+"/results/collection/onlineAnalysisResults/CH/"
  var onlineAnalysisAtPath = basePath+"/results/collection/onlineAnalysisResults/AT/"
  var onlineAnalysisFrPath = basePath+"/results/collection/onlineAnalysisResults/FR/"
  var onlineAnalysisBePath = basePath+"/results/collection/onlineAnalysisResults/BE/"
  var onlineAnalysisLuPath = basePath+"/results/collection/onlineAnalysisResults/LU/"
  var onlineAnalysisResultsPath = basePath+"/results/collection/onlineAnalysisResults/"
  var comparisonResultsPath = basePath+"/results/collection/comparisonResults/"
  var generalDataPath = basePath+"/results/collection/generalData/"
  var predictionResultsPath = basePath+"/results/collection/predictionResults/"
  
  val populationThreshold = 105500
}