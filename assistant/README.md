# LMS Assistant (Rule-based, Safe Mode)

This folder is **not compiled**. It contains editable rules/knowledge-base content that the in-app assistant loads at runtime.

## Files

- `rules.tsv` — main rule table
- `kb/` — troubleshooting articles (plain text)

## `rules.tsv` format

Tab-separated columns:

1. `priority` — integer, lower runs first
2. `match_type` — `CONTAINS` or `REGEX`
3. `pattern` — substring (for `CONTAINS`) or regex (for `REGEX`)
4. `response` — what assistant says
5. `action_type` — `NONE`, `NAVIGATE`, `REPORT_LOW_STOCK`, `REPORT_OVERDUE`, `HELP`
6. `arg1` — action arg (module/report/help key)
7. `arg2` — action arg (section, optional)

Notes:
- Lines starting with `#` are ignored.
- Blank lines are ignored.
- Matching is case-insensitive.

