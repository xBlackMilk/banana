package banana.memory.malloc;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import banana.memory.IMemAllocator;
import banana.memory.OutOfMemoryException;
import banana.memory.initializers.MemSetInitializer;


@RunWith(value = Parameterized.class)
public abstract class AbstractComputeMemoryUsageTest {

  @Parameters
  public static Collection<Object[]> data() {
    //@formatter:off
    Object[][] data = new Object[][] {
        { 100, 5, 1},
        { 100, 5, 10},
        { 100, 5, 20},
        { 100, 5, 30},
        { 100, 5, 40},
        { 100, 5, 50},
        { 100, 5, 60},
        { 100, 5, 70},
        { 2000, 3, 3},
    };
    return Arrays.asList(data);
  }
  //@formatter:on

  public AbstractComputeMemoryUsageTest(int maxBlocks, int blockSize, int allocationSize) {
    m_allocationSize = allocationSize;
    init(maxBlocks, blockSize);
    m.setDebug(true);
    m.setInitializer(new MemSetInitializer(-1));
  }

  public abstract void init(int maxBlocks, int blockSize);

  public IMemAllocator m;
  public int m_allocationSize;

  @Test
  public void testComputeMemoryUsageFor() {
    int p = m.malloc(m_allocationSize);
    int usedBytes = m.usedBlocks() * m.blockSize() * 4;
    int computedUsage = m.computeMemoryUsageFor(m_allocationSize);
    assertEquals(usedBytes, computedUsage);
    m.free(p);
  }

  @Test
  public void testMaxCapacityFor() {
    int p = m.malloc(m_allocationSize);
    int maxCapacityFor = m.maximumCapacityFor(p);
    int expected = computeExpectedCapacityFor(m_allocationSize);
    assertEquals(String.format("Alloc %d, maxCap=%d, expected=%d", m_allocationSize,
        maxCapacityFor, expected), expected, maxCapacityFor);
    m.free(p);
  }

  @Test
  public void testUsedMemoryInLoop() {
    try {
      for (int i = 0; i < m.maxBlocks(); i++) {
        int allocationSize = i * m.blockSize();
        int p = m.malloc(allocationSize);
        int computedUsedBlocks = m.computeMemoryUsageFor(allocationSize) / (m.blockSize() * 4);
        assertEquals("Allocation size " + allocationSize, m.usedBlocks(), computedUsedBlocks);
        m.free(p);
      }
    } catch (OutOfMemoryException e) {
      // expected to happen in this test.
    }
  }

  public abstract int computeExpectedCapacityFor(int size);
}
