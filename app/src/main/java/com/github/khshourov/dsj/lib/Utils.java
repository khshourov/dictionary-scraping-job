package com.github.khshourov.dsj.lib;

import java.util.List;
import java.util.stream.IntStream;

public class Utils {

  // Sonarlint issue: java:S1118
  private Utils() {
    throw new IllegalStateException("Utility class");
  }

  public static List<int[]> createPartitions(int minValue, int maxValue, int partitionSize) {
    if (minValue == maxValue) {
      return IntStream.range(0, partitionSize).mapToObj(i -> new int[] {0, 0}).toList();
    }

    // We could've swap minValue and maxValue if minValue is greater than maxValue
    // But compiler complains:
    // "Variable used in lambda expression should be final or effectively final"
    int min = Math.min(minValue, maxValue);
    int max = Math.max(minValue, maxValue);

    return IntStream.range(0, partitionSize)
        .mapToObj(
            i -> {
              int step = Math.ceilDiv((max - min + 1), partitionSize);
              int start = min + i * step;
              int end = Math.min(max, (start + step - 1));
              return new int[] {start, end};
            })
        .toList();
  }
}
