select * from membro;

select * from projeto;

select * from membros_projeto;

SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'projeto';

SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'membros_projeto';