package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.util.*;

import java.util.*;
import java.util.function.Function;

/**
 * Matcher that takes in multiple patterns
 *
 * @author Angel Chang
 */
public class MultiPatternMatcher<T> {
  Collection<SequencePattern<T>> patterns;
  SequencePatternTrigger<T> patternTrigger;

  public MultiPatternMatcher(SequencePatternTrigger<T> patternTrigger,
                             Collection<? extends SequencePattern<T>> patterns)
  {
    this.patterns = new ArrayList<SequencePattern<T>>();
    this.patterns.addAll(patterns);
    this.patternTrigger = patternTrigger;
  }

  public MultiPatternMatcher(SequencePatternTrigger<T> patternTrigger,
                             SequencePattern<T>... patterns)
  {
    this(patterns);
    this.patternTrigger = patternTrigger;
  }

  public MultiPatternMatcher(Collection<SequencePattern<T>> patterns)
  {
    this.patterns = patterns;
  }

  public MultiPatternMatcher(SequencePattern<T>... patterns)
  {
    this.patterns = new ArrayList<SequencePattern<T>>(patterns.length);
    for (SequencePattern<T> p:patterns) {
      this.patterns.add(p);
    }
  }

  /**
   * Given a sequence, applies our patterns over the sequence and returns
   *   all non overlapping matches.  When multiple patterns overlaps,
   *   matched patterns are selected by
   *     the highest priority/score is selected,
   *     then the longest pattern,
   *     then the starting offset,
   *     then the original order
   * @param elements input sequence to match against
   * @return list of match results that are non-overlapping
   */
  public List<SequenceMatchResult<T>> findNonOverlapping(List<? extends T> elements)
  {
    return findNonOverlapping(elements, SequenceMatchResult.DEFAULT_COMPARATOR);
  }

  /**
   * Given a sequence, applies our patterns over the sequence and returns
   *   all non overlapping matches.  When multiple patterns overlaps,
   *   matched patterns are selected by order specified by the comparator
   * @param elements input sequence to match against
   * @param cmp comparator indicating order that overlapped sequences should be selected.
   * @return list of match results that are non-overlapping
   */
  public List<SequenceMatchResult<T>> findNonOverlapping(List<? extends T> elements,
                                                         Comparator<? super SequenceMatchResult> cmp)
  {
    Collection<SequencePattern<T>> triggered = getTriggeredPatterns(elements);
    List<SequenceMatchResult<T>> all = new ArrayList<SequenceMatchResult<T>>();
    int i = 0;
    for (SequencePattern<T> p:triggered) {
      SequenceMatcher<T> m = p.getMatcher(elements);
      m.setOrder(i);
      while (m.find()) {
        all.add(m.toBasicSequenceMatchResult());
      }
      i++;
    }
    List<SequenceMatchResult<T>> res = IntervalTree.getNonOverlapping( all, SequenceMatchResult.TO_INTERVAL, cmp);
    Collections.sort(res, SequenceMatchResult.OFFSET_COMPARATOR);

    return res;
  }

  /**
   * Given a sequence, applies our patterns over the sequence and returns
   *   all matches, depending on the findType.  When multiple patterns overlaps,
   *   matched patterns are selected by order specified by the comparator
   * @param elements input sequence to match against
   * @param findType whether FindType.FIND_ALL or FindType.FIND_NONOVERLAPPING
   * @return list of match results
   */
  public List<SequenceMatchResult<T>> find(List<? extends T> elements, SequenceMatcher.FindType findType)
  {
    Collection<SequencePattern<T>> triggered = getTriggeredPatterns(elements);
    List<SequenceMatchResult<T>> all = new ArrayList<SequenceMatchResult<T>>();
    int i = 0;
    for (SequencePattern<T> p:triggered) {
      SequenceMatcher<T> m = p.getMatcher(elements);
      m.setFindType(findType);
      m.setOrder(i);
      while (m.find()) {
        all.add(m.toBasicSequenceMatchResult());
      }
      i++;
    }
    List<SequenceMatchResult<T>> res = IntervalTree.getNonOverlapping( all, SequenceMatchResult.TO_INTERVAL, SequenceMatchResult.DEFAULT_COMPARATOR);
    Collections.sort(res, SequenceMatchResult.OFFSET_COMPARATOR);

    return res;
  }



  /**
   * Given a sequence, applies our patterns over the sequence and returns
   *   all non overlapping matches.  When multiple patterns overlaps,
   *   matched patterns are selected to give the overall maximum score
   * @param elements input sequence to match against
   * @return list of match results that are non-overlapping
   */
  public List<SequenceMatchResult<T>> findNonOverlappingMaxScore(List<? extends T> elements)
  {
    return findNonOverlappingMaxScore(elements, SequenceMatchResult.SCORER);
  }

  /**
   * Given a sequence, applies our patterns over the sequence and returns
   *   all non overlapping matches.  When multiple patterns overlaps,
   *   matched patterns are selected to give the overall maximum score
   * @param elements input sequence to match against
   * @param scorer scorer for scoring each match
   * @return list of match results that are non-overlapping
   */
  public List<SequenceMatchResult<T>> findNonOverlappingMaxScore(List<? extends T> elements,
                                                                 Function<? super SequenceMatchResult, Double> scorer)
  {
    Collection<SequencePattern<T>> triggered = getTriggeredPatterns(elements);
    List<SequenceMatchResult<T>> all = new ArrayList<SequenceMatchResult<T>>();
    int i = 0;
    for (SequencePattern<T> p:triggered) {
      SequenceMatcher<T> m = p.getMatcher(elements);
      m.setOrder(i);
      while (m.find()) {
        all.add(m.toBasicSequenceMatchResult());
      }
      i++;
    }
    List<SequenceMatchResult<T>> res = IntervalTree.getNonOverlappingMaxScore( all, SequenceMatchResult.TO_INTERVAL, scorer);
    Collections.sort(res, SequenceMatchResult.OFFSET_COMPARATOR);

    return res;
  }

  /**
   * Given a sequence, applies each of our patterns over the sequence and returns
   *   all non overlapping matches for each of the patterns.
   * Unlike #findAllNonOverlapping, overlapping matches from different patterns are kept
   * @param elements input sequence to match against
   * @return iterable of match results that are non-overlapping
   */
  public Iterable<SequenceMatchResult<T>> findAllNonOverlappingMatchesPerPattern(List<? extends T> elements)
  {
    Collection<SequencePattern<T>> triggered = getTriggeredPatterns(elements);
    List<Iterable<SequenceMatchResult<T>>> allMatches = new ArrayList<Iterable<SequenceMatchResult<T>>>(elements.size());
    for (SequencePattern<T> p:triggered) {
      Iterable<SequenceMatchResult<T>> matches = p.getMatcher(elements).findAllNonOverlapping();
      allMatches.add(matches);
    }
    return Iterables.chain(allMatches);
  }

  /**
   * Given a sequence, return the collection of patterns that are triggered by the sequence
   *   (these patterns are the ones that may potentially match a subsequence in the sequence)
   * @param elements Input sequence
   * @return Collection of triggered patterns
   */
  public Collection<SequencePattern<T>> getTriggeredPatterns(List<? extends T> elements) {
    if (patternTrigger != null) {
      return patternTrigger.apply(elements);
    } else {
      return patterns;
    }
  }

  /** Interfaces for optimizing application of many SequencePatterns over a particular sequence */

  /**
   * A function which returns a collections of patterns that may match when
   *   given a single node from a larger sequence.
   * @param <T>
   */
  public static interface NodePatternTrigger<T> extends Function<T, Collection<SequencePattern<T>>> {}

  /**
   * A function which returns a collections of patterns that may match when
   *   a sequence of nodes.  Note that this function needs to be conservative
   *   and should return ALL patterns that may match.
   * @param <T>
   */
  public static interface SequencePatternTrigger<T> extends Function<List<? extends T>, Collection<SequencePattern<T>>> {}

  /**
   * Simple SequencePatternTrigger that looks at each node, and identifies which
   *   patterns may potentially match each node, and then aggregates (union)
   *   all these patterns together.  Original ordering of patterns is preserved.
   * @param <T>
   */
  public static class BasicSequencePatternTrigger<T> implements SequencePatternTrigger<T> {
    NodePatternTrigger<T> trigger;

    public BasicSequencePatternTrigger(NodePatternTrigger<T> trigger) {
      this.trigger = trigger;
    }

    @Override
    public Collection<SequencePattern<T>> apply(List<? extends T> elements) {
      // Use LinkedHashSet to preserve original ordering of patterns.
      Set<SequencePattern<T>> triggeredPatterns = new LinkedHashSet<SequencePattern<T>>();
      for (T node:elements) {
        Collection<SequencePattern<T>> triggered = trigger.apply(node);
        triggeredPatterns.addAll(triggered);
      }
      return triggeredPatterns;
    }
  }

}
