

package StartApplication

import Analysis.OnlineAnalysis
import Analysis.OfflineAnalysis
import Analysis.ModelTraining

class StartApplication {
   
      def main(args: Array[String]) {
        
        if(args(0).compareTo("param") == 0){
          println("   parameters are:\n")
          println("   *\"off\" for offline analysis\n")
          println("     in: arg1=path to Data, arg2=\"off\",arg3=path to learning data\n")
          println("   *\"on\" for online analysis\n")
          println("     in: arg1=path to data, arg2=\"on\"\n")
          println("   *\"training\" for offline training\n")
          println("     in: arg1=path to Data, arg2=\"off\",arg3=path to learning data\n")
          System.exit(0)
        }
        
        Configurations.setBasePath(args(0))
        
        if(args(1).compareTo("off") == 0){
          val offlineAnalysis = new OfflineAnalysis()
          if(args(2).length() == 0){
            println("path to learning data is required")
            System.exit(0)
          }
          offlineAnalysis.setLearningDataPath(args(2))
          offlineAnalysis.start()  
        } 
        else if(args(1).compareTo("on") == 0) {
          val onlineAnalysis = new OnlineAnalysis()       
          onlineAnalysis.start()
        }
        else if(args(1).compareTo("training") == 0) {
          val modelTraining = new ModelTraining()
          if(args(2).length() == 0){
            println("path to learning data is required")
            System.exit(0)
          }
          modelTraining.setLearningDataPath(args(2))
          modelTraining.start()
        }
        else {
          println("wrong parameter passed \n")
          println("   parameters are:\n")
          println("   *\"off\" for offline analysis\n")
          println("   *\"on\" for online analysis\n")
          println("   *\"training\" for offline training\n")
        }
      }
}