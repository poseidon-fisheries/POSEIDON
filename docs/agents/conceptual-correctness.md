# Conceptual correctness over convenient fixes

Never resolve a design problem by picking the solution that's merely convenient or that makes an
immediate symptom go away, when it costs conceptual correctness. If a fix would collapse two
distinct concepts into one because that's easier to code, don't — name the distinction and give it
its own representation instead, even if that means an extra class/predicate/exception.

Example: a "no path exists between two points" failure is not the same concept as "a path exists
but the destination is illegal" or "a path exists but is too slow" — collapsing all three into a
single boolean `false` (so an unreachable destination looks identical to a reachable-but-rejected
one) discards information a future reader needs, even though it's the smaller diff. The correct
shape kept "unreachable" as its own explicit failure (an exception, or a dedicated
reachability check run first) rather than silently folding it into the same signal as "reachable
but rejected."

## Absence is not a default

A common way to collapse two concepts is to give missing state a plausible value. "The vessel is
not on a trip" is not "it departed just now"; "no trip has started yet" is not "a trip that has
lasted zero time"; "it has never departed" is not "it departed long ago". Each such default makes
one check serve two different questions, and it turns a wiring error (asking about the current
trip when there is none) into a silently wrong answer.

When code reaches for a fallback (`x == null ? … : …`, `getOrDefault`, `orElse`, a sentinel date
or zero), stop and ask what the absence means:

- usually the caller is asking two different questions, which deserve two different checks (for
  example, one check for a trip that would leave now, used at departure, and another for the
  current trip, used at sea);
- or the absence is a wiring error, and the code should throw;
- or the absence is a case of its own in the domain, and it gets its own explicit representation
  (an empty `Optional`, a dedicated predicate such as "has started a trip today" that is false for
  a vessel that never made one).

A default is right only when it genuinely is the domain meaning, such as a species with no price
selling for nothing. Then say so in the Javadoc, so the next reader knows it was a decision.

## When in doubt

When you notice yourself justifying a shortcut with "this solves the problem in front of us" rather
than "this is what the concept actually is," stop and raise the conceptual-correctness question to
the user instead of silently taking the expedient path.
