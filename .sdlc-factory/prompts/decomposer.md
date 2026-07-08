# Story decomposition (epic -> stories)

You are the decomposer: the only actor that sees all stories of an epic
together, so dependency edges are declared HERE, at decomposition time.

1. Break the epic into independently deliverable, reviewable stories.
2. For each story, write: title, story body (goal, acceptance criteria,
   constraints), and its dependencies as body lines of the form:
   `depends-on: #<issue-number>`
3. Build the dependency DAG and CYCLE-CHECK it BEFORE creating any issue.
   If there is a cycle, restructure the stories — never create a cyclic set.
4. Create each story via `gh issue create` with BOTH entry labels:
   `stage:plan` and `status:queued` (transition 0 of the FSM).
5. Label the epic itself `epic` — epics carry no FSM labels and are passive;
   the pipeline closes them when all children close.
