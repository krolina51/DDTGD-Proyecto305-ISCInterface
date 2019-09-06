|=========================================
|Test MUX - DvcBogota  
|Withdrawal from CNB
|=========================================

[add flow directory "../../flows/sink"]

[execute action "Postilion.EnsureSaFQClear"]

[connect DummySrc]
[connect DvcBogotaSap1]


|
|-----------Cross Transaction----------
|


|
|In this case the field S102  the entity id is replaced  with the key SvcInstCode-> DebitID  
|and sent to remote with entity id 0036
|
[use flow _0200_CREDIT
	[send "0200" from DummySrc overrides MsgFlo.0200_Src
		[
		"3"			:	"214310"
		]
	]
	
	[receive "0200" at DvcBogotaSap1 overrides MsgFlo.0200_Snk
		[
		"3"			:	"501043"
		]
	]
]

|
|[DM-39726] - CHANGE MEGABANCO'S CODE
|
|In this case the field S102 the entity id (first 4 positions of msg sent by TM if the field S102 length is 21) 
|But is replaced with bogota entity code (0001) 
|if entity id 0036 is present
|
[use flow _0200_CREDIT

	[send "0200" from DummySrc overrides MsgFlo.0200_Src
		[
		"3"			:	"214310"
		"35"		:	"4239499002029253=14122015320000000000"
		]
	]
	
	[receive "0200" at DvcBogotaSap1 overrides MsgFlo.0200_Snk
		[
		"3"			:	"501043"
		"102"		:	"000100000000000001234"
		]

	]
]


|
|-----------Mix Transaction-----------
|

|
|[DM-39726] - CHANGE MEGABANCO'S CODE
|
|In this case the field S102 the entity id (first 4 positions of msg sent by TM if the field S102 length is 21) 
|But is replaced with bogota entity code (0001) 
|if entity id 0036 is present
|
[use flow _0200_CREDIT

	[send "0200" from DummySrc overrides MsgFlo.0200_Src
		[
		"3"			:	"401043"
		"35"		:	"4239499002029253=14122015320000000000"
		]
	]
	
	[receive "0200" at DvcBogotaSap1 overrides MsgFlo.0200_Snk
		[
		"3"			:	"501043"
		"102"		:	"000100000000000001234"
		]

	]
]

|
|[DM-39726] - CHANGE MEGABANCO'S CODE
|
|In this case the field S102 the entity id (first 4 positions of msg sent by TM if the field S102 length is 21) 
|But is replaced with bogota entity code (0001) 
|if entity id 0036 is present
|
[use flow _0200_CREDIT

	[send "0200" from DummySrc overrides MsgFlo.0200_Src
		[
		"3"			:	"401043"
		"35"		:	"4239499002029253=14122015320000000000"
		]
	]
	
	[receive "0200" at DvcBogotaSap1 overrides MsgFlo.0200_Snk
		[
		"3"			:	"501043"
		"102"		:	"000100000000000001234"
		]

	]
]


[disconnect DvcBogotaSap1]
[connect DummySrc]

