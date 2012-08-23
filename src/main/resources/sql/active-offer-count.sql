select
  count(*)
from offer
where
  active = true
  and showcase = true
  and approved = true
  and now() > launch_time
  
