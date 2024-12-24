package com.github.khshourov.dsj.lib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UtilsTest {
  @ParameterizedTest
  @MethodSource("input")
  void testCreatePartitions(int min, int max, int partitionSize, int[][] expected) {
    List<int[]> partitions = Utils.createPartitions(min, max, partitionSize);

    assertEquals(partitionSize, partitions.size());

    for (int i = 0; i < partitionSize; i++) {
      assertArrayEquals(expected[i], partitions.get(i));
    }
  }

  @Test
  void compressedStringShouldBeProducedForValidContent() {
    String content = "content";

    String compressedString = Utils.compress(content);

    assertEquals("H4sIAAAAAAAA/0vOzytJzSsBAKkwxf4HAAAA", compressedString);
  }

  static Stream<Arguments> input() {
    return Stream.of(
        arguments(
            1,
            100,
            5,
            new int[][] {
              {1, 20},
              {21, 40},
              {41, 60},
              {61, 80},
              {81, 100}
            }),
        arguments(
            1,
            102,
            5,
            new int[][] {
              {1, 21},
              {22, 42},
              {43, 63},
              {64, 84},
              {85, 102}
            }),
        arguments(1, 5, 1, new int[][] {{1, 5}}),
        arguments(0, 0, 5, new int[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
        arguments(10, 1, 2, new int[][] {{1, 5}, {6, 10}}),
        arguments(1, 1, 2, new int[][] {{1, 1}, {0, 0}}),
        arguments(1, 3, 5, new int[][] {{1, 1}, {2, 2}, {3, 3}, {0, 0}, {0, 0}}));
  }
}
