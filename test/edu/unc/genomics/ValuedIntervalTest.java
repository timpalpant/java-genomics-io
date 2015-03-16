package edu.unc.genomics;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.unc.genomics.ValuedInterval;

public class ValuedIntervalTest {

  protected final ValuedInterval watson = new ValuedInterval("chr23", 30, 40, "watson", 5);
  protected final ValuedInterval crick = new ValuedInterval("chr23", 101, 95, "crick", 3.2);

  @Test
  public void testToBed() {
    assertEquals("chr23\t29\t40\twatson\t5\t+", watson.toBed());
    assertEquals("chr23\t94\t101\tcrick\t3\t-", crick.toBed());
  }

  @Test
  public void testToBedGraph() {
    assertEquals("chr23\t29\t40\t5", watson.toBedGraph());
    assertEquals("chr23\t94\t101\t3.2", crick.toBedGraph());
  }

  @Test
  public void testToGFF() {
    assertEquals("chr23\tSpotArray\tfeature\t30\t40\t5\t+\t.\tprobe_id=watson;count=1", watson.toGFF());
    assertEquals("chr23\tSpotArray\tfeature\t95\t101\t3.2\t-\t.\tprobe_id=crick;count=1", crick.toGFF());
  }

}
