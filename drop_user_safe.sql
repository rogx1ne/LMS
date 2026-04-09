-- Kill all sessions for PRJ2531H user before dropping
BEGIN
  FOR session IN (SELECT sid, serial# FROM v$session WHERE username = 'PRJ2531H')
  LOOP
    EXECUTE IMMEDIATE 'ALTER SYSTEM KILL SESSION ''' || session.sid || ',' || session.serial# || '''';
  END LOOP;
EXCEPTION
  WHEN OTHERS THEN
    NULL;
END;
/

-- Now drop the user
BEGIN
    EXECUTE IMMEDIATE 'DROP USER PRJ2531H CASCADE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1918 THEN -- user doesn't exist
            RAISE;
        END IF;
END;
/
