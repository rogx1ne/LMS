-- Cleanup script for PRJ2531H user
-- Run as SYSTEM user: sqlplus system/oracle @cleanup_prj2531h.sql

BEGIN
  FOR sess IN (SELECT sid, serial# FROM v$session WHERE username = 'PRJ2531H') LOOP
    BEGIN
      EXECUTE IMMEDIATE 'ALTER SYSTEM KILL SESSION ''' || sess.sid || ',' || sess.serial# || ''' IMMEDIATE';
    EXCEPTION WHEN OTHERS THEN
      NULL;
    END;
  END LOOP;
EXCEPTION WHEN OTHERS THEN
  NULL;
END;
/

DROP USER PRJ2531H CASCADE;
/

COMMIT;
/

EXIT;
