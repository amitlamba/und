/*(where name==Added to cart  and
( attributes.Category Contains [hello] or attributes.Category Contains [world] )  and
creationTime Before [2018-04-28T18:30:00.000Z]  and
sumof() Equals [25]) and (where name==App Uninstalled  and
( count of event == 1 )  and
creationTime Before [2018-04-29]  and
count Equals [2]) and
 ( not (where name==Added to cart  and
( attributes.Product DoesNotContain [test] or attributes.Product Contains [hello] )  and
creationTime Before [2018-04-01T18:30:00.000Z]) )

and (UserProperties.User Property Name Contains [25] or UserProperties.User Property Name Contains [40])
 and (Demographics.age NotEquals [25-35])
 and (Technographics.Browser Contains [Chrome] or Technographics.Browser Contains [Firefox])
 and (Technographics.Device Equals [Mobile] or Technographics.Device Contains [Tablet])
 and (AppFields.OS Version Equals [9.1])
 */
/*


enum class DateOperator {
    Before, {creationTime: {$lt:new Date("2019-04-12")}}//or use isodate with time zone etc
    After,{creationTime: {$gt:new Date("2019-04-12")}}//or use isodate with time zone etc
    On,{creationTime: {$eq:new Date("2019-04-12")}}//or use isodate with time zone etc
    Between, {creationTime: {$gt:new Date("2019-04-12"), $lt:new Date("2019-10-12")}}}//or use isodate with time zone etc
    InThePast, //on exact day from today in past
    WasExactly, //range of days/weeks etc
    Today, current date
    InTheFuture, //only applicable for properties
    WillBeExactly, //om exact that day in future from today
    Exists, //field exists
    DoesNotExist //field doesnt exists
}

enum class NumberOperator {
    Equals,
    Between,
    GreaterThan,
    LessThan,
    NotEquals,
    Exists, //field exists
    DoesNotExist //field doesnt exists
}

enum class StringOperator {
    Equals,
    NotEquals,
    Contains, //check in set of values
    DoesNotContain, //check in set of values
    Exists, //field exists
    DoesNotExist //field doesnt exists
}
 */

db['2_event'].aggregate([

    {
        $addFields: {
            weekday: {$dayOfWeek: "$creationTime"},
            monthday: {$dayOfMonth: "$creationTime"},
            month: {$month: "$creationTime"},
            hour: {$hour: "$creationTime"},
            minute: {$minute: "$creationTime"},
            second: {$second: "$creationTime"},
            year: {$year: "$creationTime"},
            counter:1
        }
    },

    {
        $match: {
            //"geogrophy.country":"India",
            "weekday": {$in: [1, 3]},
            "userId": {$ne: null}
            //,"attributes.Brand":"Ferrari"
        }
    },
    {
        $project:
            {
                name: 1,
                userId: 1,
                year: 1,
                month: 1,
                monthday: 1,
                hour: 1,
                minute: 1,
                second: 1,
                weekday: 1,
                clientId: 1
            }
    }

    ,
    {
        $group: {
            _id: "$userId",
            count: {$sum: "$counter"},
            sumof: {$sum: "$clientId"}
        }
    },
    {
        $match: {
            "count": {$gt: 0},
            "sumof": {$eq: 12}
        }
    }


])




