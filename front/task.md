DONE
17. Добавить кнопку обновить на списке ивентов и тикетов
2. Вместо названия мероприятия отображается его дата 
1. Получать список категорий и тип enum с бэкенда
4. При нажатии по кнопке редактировать выводить предыдущее значение и там бага
14. На фронте слишком длинные строки обрезать
- GET /tickets/type/count
- GET /tickets/discount/sum
- GET /tickets/discount/count 
3. фронт Сделать сортировку по нескольким полям
* delete sorting


ФРОНТ 
- [] post /sell/vip/ticket-id/person-id
- [] post /sell/discount/ticket-id/person-id/discount
* post/put со вложенным event


БЭК
* Сделать фильтры и сортировку по полям на которые мы забили
* post/put со вложенным event
3. Сделать сортировку по нескольким полям
5. Нет валидации слишком длинных параметров фильтрации, нет валидации названия фильтра (в интежер не вмещается большое число и 500 падает)
6. Не найден по фильтру при кастинге энам выводить 
7. Валидация limit и offset на отрицательных значениях
8. Возвращать вложенный ивент у тикета + как в swagger описана дата
9. Refundable не может быть null
10. Такой же формат ошибок как в swagger
11. Чтобы в запросах enum можно было вводить с маленькой буквы
13. Падает на строках длиннее 255 символов
12. Валидация координат 
15. Можно сделать refundable = -562825 (должно кидать ошибку т.к. там только булин)
16. При post если отправляют вложенный event, то создавать его
18. При удалении мероприятия удалять тикеты рекурсивно
- [] post /sell/vip/ticket-id/person-id
- [] post /sell/discount/ticket-id/person-id/discount
* Проверить что ВСЕ запросы и ответы такие же как в swagger api отчете