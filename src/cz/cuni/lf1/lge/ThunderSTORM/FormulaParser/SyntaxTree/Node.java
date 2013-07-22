package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.process.FloatProcessor;

public abstract class Node {
  
    public static final int THRESHOLDING = 1;
    public static final int RESULTS_FILTERING = 2;
    
    private int nodeType;
    
    public void setNodeType(int action) {
      this.nodeType = action;
    }
    
    public int getNodeType() {
      return nodeType;
    }
    
    public boolean isThresholding() {
      return (nodeType == THRESHOLDING);
    }
    
    public boolean isResultsFiltering() {
      return (nodeType == RESULTS_FILTERING);
    }
    
    public boolean isVariable(String obj, String var) {
      if(isThresholding()) {
        for(IFilterUI f : Thresholder.getLoadedFilters()) {
          IFilter impl = f.getImplementation();
            if(impl.getFilterVarName().equals(obj)) {
                return impl.exportVariables(false).containsKey(var);
            }
        }
        return false;
      } else if(isResultsFiltering()) {
        if(obj != null) return false;
        return IJResultsTable.getResultsTable().columnExists(var);
      } else {
        throw new UnsupportedOperationException("Unsupported type of parser! Supported types are: thresholding and results filtering.");
      }
    }
    
    public RetVal getVariable(String obj, String var) {
      if(isThresholding()) {
        if(obj == null) {   // active filter this filter is already active,
          // hence there is no need to redo the filtering step, since it has been already done
          return new RetVal(Thresholder.getActiveFilter().exportVariables(false).get(var));
        } else {    // the other ones
          for(IFilterUI f : Thresholder.getLoadedFilters()) {
            IFilter impl = f.getImplementation();
            if(impl.getFilterVarName().equals(obj)) {
              return new RetVal(impl.exportVariables(true).get(var));
            }
          }
        }
        return new RetVal((FloatProcessor)null);
      } else if(isResultsFiltering()) {
        int col_idx = IJResultsTable.getResultsTable().getColumnIndex(var);
        Double [] col_data = IJResultsTable.getResultsTable().getColumnAsDoubleObjects(col_idx);
        return new RetVal(col_data);
      } else {
        throw new UnsupportedOperationException("Unsupported type of parser! Supported types are: thresholding and results filtering.");
      }
    }
    
    public abstract RetVal eval();
    public abstract void semanticScan() throws FormulaParserException;

}