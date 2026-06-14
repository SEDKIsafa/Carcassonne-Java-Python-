class Message:
    def __init__(self,rang,source,keyword,params):
        self.rang = rang
        self.source = source
        self.keyword = keyword
        self.params = params
    
    def create_message(rang,msg):
        """
        Will transform a string from the reflector into
        a Message object

        Format : ID KEYWORD PARAMS
        
        :param rang: the rank of the message
        :param str: the string to transform
        """
        #decoupe et enleve les blancs
        cutted_msg = msg.strip().split()

        #au moins deux mots
        nb_words = len(cutted_msg)
        if nb_words < 2:
            return None
        
        source = cutted_msg[0]
        keyword = cutted_msg[1]
        if nb_words > 2:
            params = cutted_msg[2:]
        else:  
            params = []
        
        return Message(rang,source,keyword,params)

    # Affichage du message
    def __str__(self):
        return f"[{self.rang}]{self.source} -> Keyword: {self.keyword} avec les paramètres : {self.params}"