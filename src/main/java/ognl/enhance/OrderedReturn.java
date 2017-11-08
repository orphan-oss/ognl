package ognl.enhance;

import ognl.Node;


/**
 * Marks an ognl expression {@link Node} as needing to have the return portion of a 
 * getter method happen in a specific part of the generated expression vs just having
 * the whole expression returned in one chunk.
 */
public interface OrderedReturn
{
    
    /**
     * Get the core expression to execute first before any return foo logic is started.
     * 
     * @return The core standalone expression that shouldn't be pre-pended with a return keyword.
     */
    String getCoreExpression();
    
    /**
     * Gets the last expression to be pre-pended with a return &lt;expression&gt; block.
     * 
     * @return The expression representing the return portion of a statement;
     */
    String getLastExpression();
}
