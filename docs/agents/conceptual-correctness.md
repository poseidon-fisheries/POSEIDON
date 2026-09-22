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

When you notice yourself justifying a shortcut with "this solves the problem in front of us" rather
than "this is what the concept actually is," stop and raise the conceptual-correctness question to
the user instead of silently taking the expedient path.
