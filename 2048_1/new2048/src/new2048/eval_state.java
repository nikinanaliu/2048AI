/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package new2048;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 *
 * @author NETLAB
 */
public class eval_state {
    private HashMap<Long,Float> trans_table; 
    private int maxdepth = 0;
    private int curdepth = 0;
    private int cachehits = 0;
    private BigDecimal moves_evaled;
    private int depth_limit = 0;
    
    public static final BigDecimal BIG_DECIMAL_ONE = new BigDecimal(1);

    public eval_state() {
        trans_table = new HashMap<>();
        moves_evaled = new BigDecimal(0);
    }

    /**
     * @return the maxdepth
     */
    public int getMaxdepth() {
        return maxdepth;
    }

    /**
     * @param maxdepth the maxdepth to set
     */
    public void setMaxdepth(int maxdepth) {
        this.maxdepth = maxdepth;
    }

    /**
     * @return the curdepth
     */
    public int getCurdepth() {
        return curdepth;
    }

    /**
     * @param curdepth the curdepth to set
     */
    public void setCurdepth(int curdepth) {
        this.curdepth = curdepth;
    }
    
    public void addCurdepth() {
        this.curdepth++;
    }
    
    public void minusCurdepth() {
        this.curdepth--;
    }

    /**
     * @return the cachehits
     */
    public int getCachehits() {
        return cachehits;
    }

    /**
     * @param cachehits the cachehits to set
     */
    public void setCachehits(int cachehits) {
        this.cachehits = cachehits;
    }
    
    public void addCachehits() {
        this.cachehits++;
    }

    /**
     * @return the moves_evaled
     */
    public BigDecimal getMoves_evaled() {
        return moves_evaled;
    }

    /**
     * @param moves_evaled the moves_evaled to set
     */
    public void setMoves_evaled(BigDecimal moves_evaled) {
        this.moves_evaled = moves_evaled;
    }
    
    public void addMoves_evaled() {
        this.moves_evaled.add(BIG_DECIMAL_ONE);
    }

    /**
     * @return the depth_limit
     */
    public int getDepth_limit() {
        return depth_limit;
    }

    /**
     * @param depth_limit the depth_limit to set
     */
    public void setDepth_limit(int depth_limit) {
        this.depth_limit = depth_limit;
    }

    /**
     * @return the trans_table
     */
    public HashMap<Long,Float> getTrans_table() {
        return trans_table;
    }

    /**
     * @param trans_table the trans_table to set
     */
    public void setTrans_table(HashMap<Long,Float> trans_table) {
        this.trans_table = trans_table;
    }
    
}
