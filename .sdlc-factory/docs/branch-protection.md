# Branch protection checklist (the real merge enforcement layer)

The pipeline squash-merges automatically once a human approves the review
gate. What actually guards main is GitHub branch protection — configure it
before going live:

- [ ] Protect the default branch (Settings -> Branches -> Add rule).
- [ ] Require status checks to pass before merging; select the CI checks.
      This one is MANDATORY, not optional: the implement session runs tests
      on its own host, so required checks are the only independent
      verification before the auto-merge. `sdlc-factory doctor` flags a
      default branch without required checks.
- [ ] Require branches to be up to date before merging.
- [ ] Do NOT allow force pushes or deletions.
- [ ] (Optional) Require pull request reviews — note the pipeline's reviewer
      agent comments but does not submit a GitHub review; if you require
      GitHub reviews, a human must approve in the GitHub UI too.

A failed merge parks the story at merge+needs-human with the reason on the
thread and NO retry loop; a human re-approves to retry.
